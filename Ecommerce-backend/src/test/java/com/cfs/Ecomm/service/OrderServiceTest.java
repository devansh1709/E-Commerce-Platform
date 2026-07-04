package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(userRepository, productRepository, orderRepository);
    }

    @Test
    void placeOrder_calculatesTotalAndDecrementsStock() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Product product = product(10L, "Wireless Mouse", "199.00", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderDTO result = orderService.placeOrder(1L, Map.of(10L, 2));

        assertThat(result.getTotalAmount()).isEqualByComparingTo("398.00");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getOrderItems()).hasSize(1);
        assertThat(product.getStock()).isEqualTo(3); // 5 - 2
        verify(productRepository).save(product);
    }

    @Test
    void placeOrder_rejectsEmptyCart() {
        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart cannot be empty");

        verifyNoInteractions(userRepository, productRepository, orderRepository);
    }

    @Test
    void placeOrder_rejectsInsufficientStock() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Product product = product(10L, "Wireless Mouse", "199.00", 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of(10L, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(99L, Map.of(10L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void placeOrder_throwsWhenProductMissing() {
        User user = user(1L, "Aarav", "aarav@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(1L, Map.of(10L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void getOrderByUser_returnsOnlyThatUsersOrders() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrderByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserName()).isEqualTo("Aarav");
    }

    @Test
    void getOrderByUser_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllOrders_mapsEveryOrderToDto() {
        User user = user(1L, "Aarav", "aarav@example.com");
        Orders order = orderWithOneItem(user);

        when(orderRepository.findAllOrdersWithUsers()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEmail()).isEqualTo("aarav@example.com");
    }

    private static User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private static Product product(Long id, String name, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal(price));
        product.setCategory("Electronics");
        product.setStock(stock);
        return product;
    }

    private static Orders orderWithOneItem(User user) {
        Product product = product(10L, "Wireless Mouse", "199.00", 5);

        Orders order = new Orders();
        order.setId(100L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("398.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        order.setOrderItems(List.of(item));

        return order;
    }
}