package com.cfs.Payment_Gateway.service;

import com.cfs.Payment_Gateway.client.RazorpayClientFactory;
import com.cfs.Payment_Gateway.dto.PaymentRequest;
import com.cfs.Payment_Gateway.dto.PaymentResponse;
import com.cfs.Payment_Gateway.entity.PaymentOrder;
import com.cfs.Payment_Gateway.repo.PaymentRepo;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepo paymentRepo;
    private RazorpayClientFactory razorpayClientFactory;
    private RazorpayClient razorpayClient;
    private OrderClient orderClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepo = mock(PaymentRepo.class);
        razorpayClientFactory = mock(RazorpayClientFactory.class);
        razorpayClient = mock(RazorpayClient.class);
        orderClient = mock(OrderClient.class);

        paymentService = new PaymentService(
                paymentRepo,
                razorpayClientFactory
        );

        ReflectionTestUtils.setField(
                paymentService,
                "keyId",
                "rzp_test_key"
        );

        ReflectionTestUtils.setField(
                paymentService,
                "keySecret",
                "test_secret"
        );
    }

    @Test
    void createOrder_createsRazorpayOrderAndSavesPayment()
            throws Exception {

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(100L);
        request.setAmount(new BigDecimal("398.00"));
        request.setCurrency("INR");
        request.setCustomerName("Aarav");
        request.setCustomerEmail("aarav@example.com");
        request.setCustomerPhone("9876543210");

        Order razorpayOrder = mock(Order.class);

        when(razorpayClientFactory.create(
                "rzp_test_key",
                "test_secret"
        )).thenReturn(razorpayClient);

        razorpayClient.orders = orderClient;

        when(orderClient.create(any(JSONObject.class)))
                .thenReturn(razorpayOrder);

        when(razorpayOrder.get("id"))
                .thenReturn("order_test_123");

        PaymentResponse response =
                paymentService.createOrder(request);

        assertThat(response.getRazorpayOrderId())
                .isEqualTo("order_test_123");

        assertThat(response.getKeyId())
                .isEqualTo("rzp_test_key");

        assertThat(response.getAmount())
                .isEqualTo(39800L);

        assertThat(response.getCurrency())
                .isEqualTo("INR");

        verify(paymentRepo).save(argThat(payment ->
                payment.getEcommerceOrderId().equals(100L)
                        && payment.getRazorpayOrderId()
                        .equals("order_test_123")
                        && payment.getAmount()
                        .compareTo(new BigDecimal("398.00")) == 0
                        && payment.getCurrency().equals("INR")
                        && payment.getCustomerName().equals("Aarav")
                        && payment.getCustomerEmail()
                        .equals("aarav@example.com")
                        && payment.getCustomerPhone()
                        .equals("9876543210")
                        && payment.getStatus().equals("CREATED")
                        && payment.getCreatedAt() != null
        ));
    }
}