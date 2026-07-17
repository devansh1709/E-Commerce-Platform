# Shoplane — E-Commerce Platform

A full-stack e-commerce platform with a two-service backend: a core commerce API and a dedicated Payment-Gateway microservice handling Razorpay integration with signature-verified payment confirmation. JWT + role-based auth, Redis-cached catalog, a Kafka-driven order/notification pipeline, Docker Compose orchestration across both services, and a Jenkins pipeline that builds, tests, and deploys both independently.

---

## Table of contents

- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Features](#features)
- [Payment & notification flow](#payment--notification-flow)
- [Getting started](#getting-started)
- [Environment variables](#environment-variables)
- [API overview](#api-overview)
- [Running tests](#running-tests)
- [CI/CD](#cicd)
- [Project structure](#project-structure)
- [Roadmap](#roadmap)

---

## Architecture

```
┌──────────────────┐        REST (JWT)        ┌───────────────────────────┐
│  React Frontend  │ ───────────────────────▶ │     Ecommerce-backend     │
│  (Vite + Axios)  │ ◀─────────────────────── │        (port 8080)        │
└──────────────────┘                          │  ┌──────────────────────┐ │
                                                │  │  Spring Security      │ │
                                                │  │   JWT + BCrypt        │ │
                                                │  └──────────────────────┘ │
                                                │  ┌──────────────────────┐ │
                                                │  │   OrderService         │ │
                                                │  │  (stock locking,       │─┼──▶ REST ──▶ ┌────────────────────┐
                                                │  │ signature verify,      │ │            │  Payment-Gateway    │
                                                │  │ publishes OrderPlaced  │ │            │     (port 8081)     │
                                                │  │  Event to Kafka)       │ │            │  Creates Razorpay   │
                                                │  └──────────┬────────────┘ │            │  orders, persists   │
                                                │             │ publish       │            │  PaymentOrder record│
                                                │             ▼               │            └──────────┬──────────┘
                                                │      Kafka (order-events)   │                       │
                                                │             │               │                       ▼
                                                │             ▼               │                 Razorpay API
                                                │  ┌──────────────────────┐   │
                                                │  │  NotificationService  │   │
                                                │  │  (@KafkaListener,      │   │
                                                │  │   sends confirmation   │   │
                                                │  │   email via Spring     │   │
                                                │  │   Mail)                │   │
                                                │  └──────────────────────┘   │
                                                └───────────┬───────────────┘
                                                             │
                                                             ▼
                                                        MySQL (ecomDB)                MySQL (pay) — same instance,
                                                                                         separate schema

                                                Redis caches product catalog reads (ProductService),
                                                evicted on every product write AND on stock changes
                                                made during checkout / cancellation.
```

**Order → payment → notification flow at a glance:** placing an order creates a `PENDING` order in `ecomDB` *and* asks Payment-Gateway to create a matching Razorpay order (stored in its own `pay` schema). The frontend opens Razorpay's checkout with that order id. On success, the frontend sends Razorpay's payment id + signature back to the e-commerce backend, which **independently verifies the HMAC signature itself** before marking the order `PAID`. Once `PAID`, `OrderService` publishes an `OrderPlacedEvent` to Kafka; `NotificationService` consumes it asynchronously and sends the confirmation email — decoupling payment confirmation from notification dispatch. If Kafka is unreachable, the publish falls back to sending the email synchronously so a paid order never silently loses its confirmation.

---

## Tech stack

**Ecommerce-backend**
- Java 21, Spring Boot 4 (`spring-boot-starter-webmvc`)
- Spring Security 6 + JWT (`jjwt`) + BCrypt, role-based access (`USER` / `ADMIN`)
- Spring Data JPA + MySQL
- Spring Mail — order confirmation emails
- Apache Kafka (`spring-kafka`) — `order-events` topic, 3 partitions, manual ack, `DefaultErrorHandler` with retry (3× / 2s backoff) before falling back to synchronous email
- Redis — product catalog caching (`spring-boot-starter-data-redis` + `spring-boot-starter-cache`, JSON serialization, 10-minute TTL), evicted on every catalog write and on stock changes made during checkout/cancellation
- springdoc-openapi — Swagger UI
- JUnit 5, Mockito, AssertJ (H2 in-memory DB for test runs; `KafkaTemplate` mocked in tests)

**Payment-Gateway** (separate Spring Boot service)
- Razorpay Java SDK — order creation
- Injectable `RazorpayClientFactory` for testable payment logic
- Its own MySQL schema (`pay`) for payment records, isolated from the commerce schema

**Frontend**
- React 18 + Vite, React Router, Axios
- Context API for auth state and cart

**Infra**
- Docker Compose — orchestrates MySQL, Redis, Kafka, Payment-Gateway, and the e-commerce backend as one stack
- Jenkins pipeline — builds, tests, and deploys **both** backend services independently, then verifies the live deployment

---

## Features

- **JWT authentication with roles** — signup/login issue signed tokens; `BCryptPasswordEncoder` for hashing; `USER`/`ADMIN` roles gate product management and the all-orders admin view.
- **Ownership-scoped orders** — every order action (`place`, `my`, `confirm-payment`, `cancel-payment`) resolves the acting user from the authenticated JWT principal, not from a client-supplied id — no endpoint trusts a caller-provided user/order id without checking it belongs to them.
- **Stock-safe checkout** — `OrderService.placeOrder` validates and decrements stock inside a single `@Transactional` method, with `@Version`-based optimistic locking on `Product` to guard against concurrent oversell.
- **Two-service Razorpay integration** — Payment-Gateway creates the Razorpay order and stores a payment record; the e-commerce backend independently verifies the payment signature (`Utils.verifySignature`) before trusting a payment as successful.
- **Kafka-driven order notifications** — `OrderService` publishes an `OrderPlacedEvent` to the `order-events` topic after a verified payment; `NotificationService` consumes it via `@KafkaListener` and sends the confirmation email through Spring Mail, decoupling payment confirmation from notification dispatch. Failed consumer messages retry 3 times before being logged and dropped; a failed publish falls back to a synchronous email send.
- **Cancel & restock** — cancelling a pending order returns reserved stock to inventory and evicts the product cache.
- **Redis-cached product catalog** — `ProductService` caches catalog reads (`@Cacheable`) with a 10-minute TTL and evicts on writes (`@CacheEvict`); `OrderService` also triggers `evictProductCache()` after checkout and cancellation so stock changes are never served stale.
- **API documentation** — Swagger UI generated from the live controllers.
- **Full CI/CD** — a single Jenkins pipeline builds, tests, containerizes, and deploys both backend services together, then runs a live health check against the deployed stack.

---

## Payment & notification flow

1. **Checkout** — `POST /orders/place` creates a `PENDING` order, decrements stock (evicting the product cache), and calls Payment-Gateway to create a matching Razorpay order; the response includes the Razorpay order id.
2. **Checkout widget** — the frontend loads Razorpay's script and opens the checkout modal using that order id.
3. **Confirm** — on success, the frontend `POST`s `razorpay_order_id` / `razorpay_payment_id` / `razorpay_signature` to `/orders/{orderId}/confirm-payment`.
4. **Verify** — the e-commerce backend checks the Razorpay order id matches the stored one, then verifies the HMAC signature itself using the shared `razorpay.key_secret`. Only a valid signature flips the order to `PAID`; anything else marks it `FAILED`.
5. **Publish** — once `PAID`, `OrderService` publishes an `OrderPlacedEvent` to Kafka's `order-events` topic.
6. **Notify** — `NotificationService`, a `@KafkaListener` on that topic, consumes the event and sends the confirmation email via Spring Mail. If the Kafka publish itself fails, the email is sent synchronously instead so the order's confirmation is never silently lost.
7. **Cancel path** — a still-`PENDING` order can be cancelled via `/orders/{orderId}/cancel-payment`, which restocks the reserved items and evicts the product cache.

---

## Getting started

### Prerequisites
- JDK 21, Node.js 18+, Docker & Docker Compose

### Run everything with Docker Compose (recommended)
```bash
# create a .env file in the repo root with MYSQL_*, JWT_SECRET, RAZORPAY_*, MAIL_* — see below
docker compose up -d
```
This brings up MySQL (with both `ecomDB` and `pay` schemas via `mysql-init/`), Redis, Kafka, Payment-Gateway on `:8081`, and the e-commerce backend on `:8090` (mapped from its internal `:8080`) — with health checks gating startup order (backend waits on MySQL + Redis + Kafka, Payment-Gateway waits on MySQL).

### Run locally without Docker
```bash
# 1. Start MySQL locally with an `ecomDB` and a `pay` schema, plus a local Redis and Kafka broker

# 2. Payment-Gateway
cd Payment-Gateway
export DB_USER=root DB_PASS=yourpassword
export RAZORPAY_KEY_ID=rzp_test_xxx RAZORPAY_KEY_SECRET=xxx
./mvnw spring-boot:run   # starts on :8081

# 3. Ecommerce-backend (new terminal)
cd Ecommerce-backend
export JWT_SECRET=$(openssl rand -base64 48)
export RAZORPAY_KEY_ID=rzp_test_xxx RAZORPAY_KEY_SECRET=xxx   # must match Payment-Gateway
export MAIL_USERNAME=you@gmail.com MAIL_APP_PASSWORD=your-gmail-app-password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
./mvnw spring-boot:run   # starts on :8080

# 4. Frontend (new terminal)
cd Ecommerce-frontend
npm install
npm run dev              # http://localhost:5173
```

### Explore the API
Once running, open **`http://localhost:8080/swagger-ui.html`** (or `:8090` under Docker Compose) for interactive API docs.

---

## Environment variables

| Variable | Used by | Purpose |
|---|---|---|
| `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD` | Docker Compose | MySQL credentials for the shared instance |
| `DB_URL` / `DB_USER` / `DB_PASS` | Both services | Direct DB connection when not using Compose |
| `JWT_SECRET` | Ecommerce-backend | HMAC signing key for auth tokens (32+ chars) |
| `JWT_EXPIRATION_MS` | Ecommerce-backend | Token lifetime, default 24h |
| `PAYMENT_GATEWAY_URL` | Ecommerce-backend | Base URL for the Payment-Gateway service |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | **Both services** | Must be identical on both sides — signature verification depends on it |
| `MAIL_USERNAME` / `MAIL_APP_PASSWORD` | Ecommerce-backend | Gmail SMTP credentials for order confirmation emails (use a Gmail **App Password**, not your login password) |
| `REDIS_HOST` / `REDIS_PORT` | Ecommerce-backend | Redis connection for product catalog caching, defaults to `localhost:6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Ecommerce-backend | Kafka broker address for the order-events producer/consumer, defaults to `localhost:9092` |

> Never commit real values for any of these — both services read them from the environment with no hardcoded fallbacks.

---

## API overview

**Ecommerce-backend** (`:8080`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/users/register` | Public | Create an account, returns a JWT |
| `POST` | `/users/login` | Public | Authenticate, returns a JWT |
| `GET` | `/products` \| `/products/{id}` \| `/products/category/{c}` \| `/products/search` | Public | Browse/search the catalog |
| `POST`/`PUT`/`DELETE` | `/products/**` | Admin | Manage the catalog |
| `POST` | `/orders/place` | JWT | Place an order, initiates a Razorpay order |
| `GET` | `/orders/my` | JWT | The caller's own orders |
| `GET` | `/orders/all-orders` | Admin | All orders |
| `POST` | `/orders/{id}/confirm-payment` | JWT (owner only) | Verify Razorpay signature, mark order `PAID`/`FAILED`, publish `OrderPlacedEvent` |
| `POST` | `/orders/{id}/cancel-payment` | JWT (owner only) | Cancel a pending order and restock |

**Payment-Gateway** (`:8081`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/create-order` | Creates a Razorpay order and stores a payment record (called internally by Ecommerce-backend) |

Full schemas for both services are available via Swagger UI at runtime.

---

## Running tests

```bash
cd Ecommerce-backend && ./mvnw test
cd Payment-Gateway && ./mvnw test
```
- `OrderServiceTest` covers stock decrementing, empty-cart/insufficient-stock rejection, the full `confirmPayment` path (missing order, mismatched Razorpay order id, invalid signature, valid signature), and verifies `orderKafkaTemplate.send(...)` is invoked on a successful order (Kafka is mocked, not a live broker).
- `PaymentServiceTest` mocks `RazorpayClientFactory` to verify order creation and payment-record persistence without calling the real Razorpay API.

---

## CI/CD

A `Dockerfile.jenkins` builds a Jenkins agent image (`jenkins/jenkins:lts-jdk21` + Docker CLI) capable of running the pipeline's own `docker build`/`docker compose` steps.

The `Jenkinsfile` at the repo root runs a single pipeline across **both** services:

1. Build + test `Ecommerce-backend` (JUnit results published)
2. Build + test `Payment-Gateway` (JUnit results published)
3. Package both jars
4. Docker build both images
5. Validate the Compose file (`docker-compose config`) with dummy env values
6. Push both images to Docker Hub (`dockerhub-credentials`)
7. Deploy the full stack via `docker compose up -d` using a stored env file (`shoplane-compose-env`)
8. **Verify the live deployment** with a health-check `curl` against `/products` on the deployed backend, retrying until it responds

Required Jenkins credentials: `dockerhub-credentials` (Docker Hub username/token) and `shoplane-compose-env` (a secret file with the real `.env` values for deployment).

---

## Project structure

```
Ecommerce-backend/
├── security/      # JwtFilter, JwtUtil, SecurityConfig
├── controller/    # UserController, ProductController, OrderController
├── service/       # OrderService (checkout, payment verification, Kafka publish), ProductService (catalog + caching),
│                  # EmailNotificationService (synchronous fallback), NotificationService (Kafka consumer)
├── config/        # KafkaProducerConfig, KafkaConsumerConfig
├── client/        # PaymentGatewayClient (REST client to Payment-Gateway)
├── model/         # User, Product, Orders, OrderItem
├── dto/           # PaymentRequest/Response/ConfirmationRequest, OrderDTO, OrderPlacedEvent, etc.
Payment-Gateway/
├── client/        # RazorpayClientFactory (injectable, testable)
├── controller/    # PaymentController
├── service/       # PaymentService
├── entity/        # PaymentOrder
Ecommerce-frontend/
├── src/pages/      # Home, Cart, Orders, Admin, Login, Register
├── src/context/    # AuthContext, CartContext
```

---

## Roadmap

- [ ] Dead-letter topic for `order-events` — currently a permanently-failing consumer message is retried 3× then just logged and dropped; routing it to a DLQ topic instead would make failures replayable/inspectable.
- [ ] Pagination for product search/listing
- [ ] Rate limiting on `/users/login` and `/users/register`
- [ ] Idempotency guard on `NotificationService` — a Kafka redelivery (e.g. after a consumer crash before offset commit) could send a duplicate confirmation email; deduplicating on `orderId` would close that gap.
