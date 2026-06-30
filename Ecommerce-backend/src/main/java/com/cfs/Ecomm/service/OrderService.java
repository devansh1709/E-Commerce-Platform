package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderDTO;
import com.cfs.Ecomm.dto.OrderItemDTO;
import com.cfs.Ecomm.enums.OrderStatus;
import com.cfs.Ecomm.exception.BadRequestException;
import com.cfs.Ecomm.exception.ResourceNotFoundException;
import com.cfs.Ecomm.model.OrderItem;
import com.cfs.Ecomm.model.Orders;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.OrderRepository;
import com.cfs.Ecomm.repo.ProductRepository;
import com.cfs.Ecomm.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities) {

        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new BadRequestException("Cart cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Orders order = new Orders();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemDTO> orderItemDTOS = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {

            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            int quantity = entry.getValue();

            if (product.getStock() < quantity) {
                throw new BadRequestException(
                        "Insufficient stock for: " + product.getName()
                );
            }

            product.setStock(product.getStock() - quantity);

            productRepository.save(product);

            if (quantity <= 0) {
                throw new BadRequestException("Quantity must be greater than zero");
            }

            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);

            orderItems.add(orderItem);

            orderItemDTOS.add(
                    new OrderItemDTO(
                            product.getName(),
                            product.getPrice(),
                            quantity
                    )
            );
        }

        order.setTotalAmount(total);
        order.setOrderItems(orderItems);

        Orders savedOrder = orderRepository.save(order);

        return new OrderDTO(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                orderItemDTOS
        );
    }

    public List<OrderDTO> getAllOrders(){
        List<Orders> orders=orderRepository.findAllOrdersWithUsers();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Orders orders) {
        List<OrderItemDTO> orderItems=orders.getOrderItems().stream()
                .map(item-> new OrderItemDTO(
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity())).collect(Collectors.toList());
        return new OrderDTO(
                orders.getId(),
                orders.getTotalAmount(),
                orders.getStatus(),
                orders.getOrderDate(),
                orders.getUser()!=null?orders.getUser().getName(): "Unknown",
                orders.getUser()!=null?orders.getUser().getEmail(): "Unknown",
                orderItems
        );
    }

    public List<OrderDTO> getOrderByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        List<Orders> ordersList=orderRepository.findByUser(user);
        return ordersList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
