package com.cfs.Payment_Gateway.client;

import com.razorpay.RazorpayClient;
import org.springframework.stereotype.Component;

@Component
public class RazorpayClientFactory {

    public RazorpayClient create(
            String keyId,
            String keySecret) throws Exception {

        return new RazorpayClient(keyId, keySecret);
    }
}