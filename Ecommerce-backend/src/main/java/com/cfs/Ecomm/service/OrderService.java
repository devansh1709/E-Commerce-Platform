package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderDTO;
import com.cfs.Ecomm.dto.OrderItemDTO;
import com.cfs.Ecomm.model.OrderItem;
import com.cfs.Ecomm.model.Orders;
import com.cfs.Ecomm.model.Product;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.OrderRepository;
import com.cfs.Ecomm.repo.ProductRepository;
import com.cfs.Ecomm.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities, double totalAmount){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        Orders order=new Orders();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus("Pending");
        order.setTotalAmount(totalAmount);

        List<OrderItem> orderItems=new ArrayList<>();
        List<OrderItemDTO> orderItemDTOS=new ArrayList<>();

        for (Map.Entry<Long, Integer> entry: productQuantities.entrySet()){
            Product product=productRepository.findById(entry.getKey())
                    .orElseThrow(()-> new RuntimeException("Product not found"));
            OrderItem orderItem=new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(entry.getValue());
            orderItems.add(orderItem);
            orderItemDTOS.add(new OrderItemDTO(product.getName(), product.getPrice(), entry.getValue()));
        }
        order.setOrderItems(orderItems);
        Orders saveOrder=orderRepository.save(order);
        return new OrderDTO(saveOrder.getId(), saveOrder.getTotalAmount(), saveOrder.getStatus(), saveOrder.getOrderDate(), orderItemDTOS);

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
        Optional<User> userOp = userRepository.findById(userId);
        if(userOp.isEmpty()){
            throw new RuntimeException("user not found");
        }
        User user=userOp.get();
        List<Orders> ordersList=orderRepository.findByUser(user);
        return ordersList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
