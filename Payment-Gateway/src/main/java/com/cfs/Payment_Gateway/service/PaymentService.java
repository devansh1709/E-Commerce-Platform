package com.cfs.Payment_Gateway.service;

import com.cfs.Payment_Gateway.dto.PaymentRequest;
import com.cfs.Payment_Gateway.dto.PaymentResponse;
import com.cfs.Payment_Gateway.entity.PaymentOrder;
import com.cfs.Payment_Gateway.repo.PaymentRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.cfs.Payment_Gateway.client.RazorpayClientFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final RazorpayClientFactory razorpayClientFactory;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    public PaymentService(
            PaymentRepo paymentRepo,
            RazorpayClientFactory razorpayClientFactory) {

        this.paymentRepo = paymentRepo;
        this.razorpayClientFactory = razorpayClientFactory;
    }
    public PaymentResponse createOrder(
            PaymentRequest request) throws Exception {

        RazorpayClient client =
                razorpayClientFactory.create(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();

        long amountInPaise = request.getAmount()
                .movePointRight(2)
                .longValueExact();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", request.getCurrency());
        orderRequest.put(
                "receipt",
                "order_" + request.getOrderId()
        );

        Order razorpayOrder =
                client.orders.create(orderRequest);

        String razorpayOrderId =
                razorpayOrder.get("id");

        PaymentOrder paymentOrder = new PaymentOrder();

        paymentOrder.setEcommerceOrderId(request.getOrderId());
        paymentOrder.setRazorpayOrderId(razorpayOrderId);
        paymentOrder.setAmount(request.getAmount());
        paymentOrder.setCurrency(request.getCurrency());
        paymentOrder.setCustomerName(request.getCustomerName());
        paymentOrder.setCustomerEmail(request.getCustomerEmail());
        paymentOrder.setCustomerPhone(request.getCustomerPhone());
        paymentOrder.setStatus("CREATED");
        paymentOrder.setCreatedAt(LocalDateTime.now());

        paymentRepo.save(paymentOrder);

        return new PaymentResponse(
                razorpayOrderId,
                keyId,
                amountInPaise,
                request.getCurrency()
        );
    }
}
