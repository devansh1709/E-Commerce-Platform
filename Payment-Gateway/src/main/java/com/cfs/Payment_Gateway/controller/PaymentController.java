package com.cfs.Payment_Gateway.controller;

import com.cfs.Payment_Gateway.dto.PaymentRequest;
import com.cfs.Payment_Gateway.dto.PaymentResponse;
import com.cfs.Payment_Gateway.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createOrder(
            @RequestBody PaymentRequest request) throws Exception {

        return ResponseEntity.ok(
                paymentService.createOrder(request)
        );
    }
}