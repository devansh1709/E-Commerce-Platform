package com.cfs.Ecomm.dto;

import com.cfs.Ecomm.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String userName;
    private String email;
    private List<OrderItemDTO> orderItems;

    private String razorpayOrderId;

    public OrderDTO(Long id, BigDecimal totalAmount, OrderStatus status, LocalDateTime orderDate, String userName, String email, List<OrderItemDTO> orderItems, String razorpayOrderId) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.userName = userName;
        this.email = email;
        this.orderItems = orderItems;
        this.razorpayOrderId=razorpayOrderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }
}
