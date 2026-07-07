package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderDTO;
import com.cfs.Ecomm.dto.PaymentRequest;
import com.cfs.Ecomm.dto.PaymentResponse;
import com.cfs.Ecomm.enums.OrderStatus;
import com.cfs.Ecomm.exception.BadRequestException;
import com.cfs.Ecomm.exception.ForbiddenException;
import com.cfs.Ecomm.exception.ResourceNotFoundException;
import com.cfs.Ecomm.model.OrderItem;
import com.cfs.Ecomm.model.Orders;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.OrderRepository;
import com.cfs.Ecomm.repo.ProductRepository;
import com.cfs.Ecomm.repo.UserRepository;
import com.cfs.Ecomm.client.PaymentGatewayClient;
import com.cfs.Ecomm.dto.PaymentConfirmationRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private OrderService orderService;
    private PaymentGatewayClient paymentGatewayClient;
    private EmailNotificationService emailNotificationService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        paymentGatewayClient = mock(PaymentGatewayClient.class);
        emailNotificationService = mock(EmailNotificationService.class);
        productService=mock(ProductService.class);

        orderService = new OrderService(
                userRepository,
                productRepository,
                orderRepository,
                paymentGatewayClient,
                emailNotificationService,
                productService
        );
        ReflectionTestUtils.setField(
                orderService,
                "razorpayKeySecret",
                "test-secret"
        );

    }

    @Test
    void placeOrder_calculatesTotalAndDecrementsStock() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Product product = product(10L, "Wireless Mouse", "199.00", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setRazorpayOrderId("order_test_123");

        when(paymentGatewayClient.createOrder(any(PaymentRequest.class)))
                .thenReturn(paymentResponse);

        OrderDTO result = orderService.placeOrder(1L, Map.of(10L, 2));

        assertThat(result.getTotalAmount()).isEqualByComparingTo("398.00");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getOrderItems()).hasSize(1);
        assertThat(product.getStock()).isEqualTo(3);

        assertThat(result.getRazorpayOrderId())
                .isEqualTo("order_test_123");

        verify(productRepository).save(product);
        verify(productService).evictProductCache();

        ArgumentCaptor<PaymentRequest> paymentRequestCaptor =
                ArgumentCaptor.forClass(PaymentRequest.class);

        verify(paymentGatewayClient)
                .createOrder(paymentRequestCaptor.capture());

        PaymentRequest capturedRequest = paymentRequestCaptor.getValue();

        assertThat(capturedRequest.getCustomerName())
                .isEqualTo("Aarav");

        assertThat(capturedRequest.getCustomerEmail())
                .isEqualTo("aarav@example.com");


        assertThat(capturedRequest.getCustomerPhone())
                .isEqualTo("9876543210");

        assertThat(capturedRequest.getAmount())
                .isEqualByComparingTo("398.00");

        assertThat(capturedRequest.getCurrency())
                .isEqualTo("INR");
    }

    @Test
    void placeOrder_rejectsEmptyCart() {
        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart cannot be empty");

        verifyNoInteractions(userRepository, productRepository, orderRepository);
    }

    @Test
    void placeOrder_rejectsInsufficientStock() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Product product = product(10L, "Wireless Mouse", "199.00", 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of(10L, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(99L, Map.of(10L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void placeOrder_throwsWhenProductMissing() {
        User user = user(1L, "Aarav", "aarav@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of(10L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void getOrderByUser_returnsOnlyThatUsersOrders() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrderByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserName()).isEqualTo("Aarav");
    }

    @Test
    void getOrderByUser_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllOrders_mapsEveryOrderToDto() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);

        when(orderRepository.findAllOrdersWithUsers()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEmail()).isEqualTo("aarav@example.com");
    }

    @Test
    void confirmPayment_throwsWhenOrderMissing() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        PaymentConfirmationRequest request =
                new PaymentConfirmationRequest();

        assertThatThrownBy(() ->
                orderService.confirmPayment(999L, 101L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmPayment_marksOrderFailedForInvalidSignature() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);
        order.setRazorpayOrderId("order_test_123");

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Orders.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentConfirmationRequest request =
                new PaymentConfirmationRequest();

        request.setRazorpayOrderId("order_test_123");
        request.setRazorpayPaymentId("pay_test_456");
        request.setRazorpaySignature("invalid_signature");

        OrderDTO result =
                orderService.confirmPayment(100L, 1L, request);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatus.FAILED);

        assertThat(order.getRazorpayPaymentId())
                .isNull();

        verify(orderRepository).save(order);
    }

    @Test
    void confirmPayment_rejectsMismatchedRazorpayOrderId() {
        User user = user(1L, "Aarav", "aarav@example.com");

        Orders order = orderWithOneItem(user);
        order.setRazorpayOrderId("order_real_123");

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        PaymentConfirmationRequest request =
                new PaymentConfirmationRequest();

        request.setRazorpayOrderId("order_different_999");
        request.setRazorpayPaymentId("pay_test_456");
        request.setRazorpaySignature("some_signature");

        assertThatThrownBy(() ->
                orderService.confirmPayment(100L, 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(
                        "Razorpay order ID does not match"
                );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmPayment_marksOrderPaidForValidSignature() throws Exception {
        User user = user(1L, "Aarav", "aarav@example.com");

        Orders order = orderWithOneItem(user);
        order.setRazorpayOrderId("order_test_123");

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Orders.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String razorpayOrderId = "order_test_123";
        String razorpayPaymentId = "pay_test_456";

        String payload =
                razorpayOrderId + "|" + razorpayPaymentId;

        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(new SecretKeySpec(
                "test-secret".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        ));

        String validSignature = HexFormat.of()
                .formatHex(
                        mac.doFinal(
                                payload.getBytes(StandardCharsets.UTF_8)
                        )
                );

        PaymentConfirmationRequest request =
                new PaymentConfirmationRequest();

        request.setRazorpayOrderId(razorpayOrderId);
        request.setRazorpayPaymentId(razorpayPaymentId);
        request.setRazorpaySignature(validSignature);

        OrderDTO result =
                orderService.confirmPayment(100L, 1L, request);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatus.PAID);

        assertThat(order.getRazorpayPaymentId())
                .isEqualTo("pay_test_456");

        verify(orderRepository).save(order);
    }

    @Test
    void confirmPayment_rejectsDifferentUser() {
        User owner = user(
                1L,
                "Aarav",
                "aarav@example.com"
        );

        Orders order = orderWithOneItem(owner);
        order.setRazorpayOrderId("order_test_123");

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        PaymentConfirmationRequest request =
                new PaymentConfirmationRequest();

        assertThatThrownBy(() ->
                orderService.confirmPayment(
                        100L,
                        999L,
                        request
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(
                        "You do not have access to this order"
                );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelPendingOrder_rejectsDifferentUser() {
        User owner = user(
                1L,
                "Aarav",
                "aarav@example.com"
        );

        Orders order = orderWithOneItem(owner);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.cancelPendingOrder(
                        100L,
                        999L
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(
                        "You do not have access to this order"
                );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelPendingOrder_restoresStockAndEvictsCache() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);
        Product product = order.getOrderItems().getFirst().getProduct();

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Orders.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.cancelPendingOrder(100L, 1L);

        assertThat(product.getStock()).isEqualTo(7);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.FAILED);

        verify(productRepository).save(product);
        verify(productService).evictProductCache();
        verify(orderRepository).save(order);
    }

    private static User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPhone("9876543210");
        return user;
    }

    private static Product product(Long id, String name, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal(price));
        product.setCategory("Electronics");
        product.setStock(stock);
        return product;
    }

    private static Orders orderWithOneItem(User user) {
        Product product = product(10L, "Wireless Mouse", "199.00", 5);

        Orders order = new Orders();
        order.setId(100L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("398.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        return order;
    }
}
