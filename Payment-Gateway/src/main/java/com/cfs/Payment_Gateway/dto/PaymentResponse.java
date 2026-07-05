package com.cfs.Payment_Gateway.dto;

public class PaymentResponse {

    private String razorpayOrderId;
    private String keyId;
    private Long amount;
    private String currency;

    public PaymentResponse(
            String razorpayOrderId,
            String keyId,
            Long amount,
            String currency) {

        this.razorpayOrderId = razorpayOrderId;
        this.keyId = keyId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getKeyId() {
        return keyId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
