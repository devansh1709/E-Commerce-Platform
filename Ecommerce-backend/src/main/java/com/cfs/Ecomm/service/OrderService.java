package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderDTO;
import com.cfs.Ecomm.dto.OrderItemDTO;
import com.cfs.Ecomm.enums.OrderStatus;
import com.cfs.Ecomm.exception.BadRequestException;
import com.cfs.Ecomm.exception.ResourceNotFoundException;
import com.cfs.Ecomm.model.OrderItem;
import com.cfs.Ecomm.model.Orders;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.OrderRepository;
import com.cfs.Ecomm.repo.ProductRepository;
import com.cfs.Ecomm.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cfs.Ecomm.client.PaymentGatewayClient;
import com.cfs.Ecomm.dto.PaymentConfirmationRequest;
import com.cfs.Ecomm.dto.PaymentRequest;
import com.cfs.Ecomm.dto.PaymentResponse;
import com.cfs.Ecomm.exception.ForbiddenException;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final EmailNotificationService emailNotificationService;
    private final ProductService productService;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    public OrderService(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PaymentGatewayClient paymentGatewayClient,
            EmailNotificationService emailNotificationService,
            ProductService productService) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentGatewayClient = paymentGatewayClient;
        this.emailNotificationService=emailNotificationService;
        this.productService = productService;
    }

    @Transactional
    public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities) {

        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new BadRequestException("Cart cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Orders order = new Orders();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemDTO> orderItemDTOS = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {

            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            int quantity = entry.getValue();

            if (quantity <= 0) {
                throw new BadRequestException("Quantity must be greater than zero");
            }


            if (product.getStock() < quantity) {
                throw new BadRequestException(
                        "Insufficient stock for: " + product.getName()
                );
            }

            product.setStock(product.getStock() - quantity);

            productRepository.save(product);


            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);

            orderItems.add(orderItem);

            orderItemDTOS.add(
                    new OrderItemDTO(
                            product.getName(),
                            product.getPrice(),
                            quantity
                    )
            );
        }

        productService.evictProductCache();

        order.setTotalAmount(total);
        order.setOrderItems(orderItems);

        Orders savedOrder = orderRepository.save(order);

        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(savedOrder.getId());
            paymentRequest.setAmount(savedOrder.getTotalAmount());
            paymentRequest.setCurrency("INR");
            paymentRequest.setCustomerName(user.getName());
            paymentRequest.setCustomerEmail(user.getEmail());
            paymentRequest.setCustomerPhone(user.getPhone());

            PaymentResponse paymentResponse =
                    paymentGatewayClient.createOrder(paymentRequest);

            savedOrder.setRazorpayOrderId(paymentResponse.getRazorpayOrderId());
            savedOrder = orderRepository.save(savedOrder);

        } catch (Exception e) {
            System.err.println(
                    "Payment order creation failed for order "
                            + savedOrder.getId()
                            + ": "
                            + e.getMessage()
            );
        }

        return new OrderDTO(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                user.getName(),
                user.getEmail(),
                orderItemDTOS,
                savedOrder.getRazorpayOrderId()
        );
    }


    @Transactional
    public OrderDTO confirmPayment(
            Long orderId,
            Long userId,
            PaymentConfirmationRequest request) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (order.getUser() == null ||
                !order.getUser().getId().equals(userId)) {

            throw new ForbiddenException(
                    "You do not have access to this order"
            );
        }

        if (order.getRazorpayOrderId() == null ||
                !order.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {

            throw new BadRequestException(
                    "Razorpay order ID does not match this order"
            );
        }

        try {
            String payload =
                    request.getRazorpayOrderId()
                            + "|"
                            + request.getRazorpayPaymentId();


            boolean valid = Utils.verifySignature(
                    payload,
                    request.getRazorpaySignature(),
                    razorpayKeySecret
            );


            if (valid) {
                order.setStatus(OrderStatus.PAID);
                order.setRazorpayPaymentId(
                        request.getRazorpayPaymentId()
                );
            } else {
                order.setStatus(OrderStatus.FAILED);
            }

        } catch (Exception e) {
            order.setStatus(OrderStatus.FAILED);
        }

        Orders savedOrder = orderRepository.save(order);

        if (savedOrder.getStatus() == OrderStatus.PAID) {
            try {
                emailNotificationService.sendOrderConfirmation(savedOrder);
            } catch (Exception e) {
                System.err.println(
                        "Order " + savedOrder.getId()
                                + " was paid, but confirmation email failed: "
                                + e.getMessage()
                );
            }
        }

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO cancelPendingOrder(
            Long orderId,
            Long userId) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (order.getUser() == null ||
                !order.getUser().getId().equals(userId)) {

            throw new ForbiddenException(
                    "You do not have access to this order"
            );
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending orders can be cancelled"
            );
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();

            product.setStock(
                    product.getStock() + item.getQuantity()
            );

            productRepository.save(product);
        }
        productService.evictProductCache();


        order.setStatus(OrderStatus.FAILED);

        Orders savedOrder = orderRepository.save(order);

        return convertToDTO(savedOrder);
    }

    public List<OrderDTO> getAllOrders(){
        List<Orders> orders=orderRepository.findAllOrdersWithUsers();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Orders orders) {
        List<OrderItemDTO> orderItems=orders.getOrderItems().stream()
                .map(item-> new OrderItemDTO(
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity())).collect(Collectors.toList());
        return new OrderDTO(
                orders.getId(),
                orders.getTotalAmount(),
                orders.getStatus(),
                orders.getOrderDate(),
                orders.getUser() != null ? orders.getUser().getName() : "Unknown",
                orders.getUser() != null ? orders.getUser().getEmail() : "Unknown",
                orderItems,
                orders.getRazorpayOrderId()
        );
    }

    public List<OrderDTO> getOrderByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        List<Orders> ordersList=orderRepository.findByUser(user);
        return ordersList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
