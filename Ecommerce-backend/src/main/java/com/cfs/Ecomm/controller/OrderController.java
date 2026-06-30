package com.cfs.Ecomm.controller;

import com.cfs.Ecomm.dto.OrderDTO;
import com.cfs.Ecomm.model.OrderRequest;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderDTO> placeOrder(
            @AuthenticationPrincipal User currentUser,
            @RequestBody OrderRequest orderRequest) {

        OrderDTO order = orderService.placeOrder(
                currentUser.getId(),
                orderRequest.getProductQuantities()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal User currentUser) {

        List<OrderDTO> orders = orderService.getOrderByUser(currentUser.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all-orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
