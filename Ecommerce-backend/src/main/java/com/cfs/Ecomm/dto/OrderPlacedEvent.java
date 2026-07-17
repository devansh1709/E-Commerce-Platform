package com.cfs.Ecomm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderPlacedEvent {

    private Long orderId;
    private String userEmail;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;

    public OrderPlacedEvent(){

    }

    public OrderPlacedEvent(Long orderId, String userEmail, String userName, BigDecimal totalAmount, String status, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
