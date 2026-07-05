package com.cfs.Ecomm.client;

import com.cfs.Ecomm.dto.PaymentRequest;
import com.cfs.Ecomm.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGatewayClient {

    private final RestClient restClient;

    public PaymentGatewayClient(
            @Value("${payment.gateway.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public PaymentResponse createOrder(PaymentRequest request) {
        return restClient.post()
                .uri("/api/payments/create-order")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
    }
}