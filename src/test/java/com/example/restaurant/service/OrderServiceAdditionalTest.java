package com.example.restaurant.service;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.User;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderItemRepository;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceAdditionalTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private RestaurantEventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).email("c@test.com").role(User.UserRole.CUSTOMER).active(true).build();
        menuItem = MenuItem.builder().id(1L).name("Burger").price(new BigDecimal("5.00")).available(true).build();
    }

    @Test
    void createOrder_menuItemNotFound_throws() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .deliveryAddress("123")
                .deliveryType("DELIVERY")
                .items(List.of(CreateOrderRequest.OrderItemRequest.builder().menuItemId(99L).quantity(1).build()))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(10L);
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return o;
        });

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_menuItemUnavailable_throws() {
        MenuItem unavailable = MenuItem.builder().id(2L).name("Pizza").price(new BigDecimal("7.00")).available(false).build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .deliveryAddress("123")
                .deliveryType("DELIVERY")
                .items(List.of(CreateOrderRequest.OrderItemRequest.builder().menuItemId(2L).quantity(1).build()))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(unavailable));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(11L);
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return o;
        });

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void updateOrderStatus_delivered_setsCompletedAt() {
        Order order = Order.builder().id(5L).status(Order.OrderStatus.PENDING).build();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.updateOrderStatus(5L, "DELIVERED");

        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getCompletedAt());
    }

    @Test
    void getCustomerOrders_returnsPage() {
        Order order = Order.builder().id(1L).customer(customer).status(Order.OrderStatus.CONFIRMED).totalAmount(new BigDecimal("5.00")).build();
        Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(orderRepository.findByCustomerId(eq(1L), any())).thenReturn(page);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of());

        Page<OrderResponse> responses = orderService.getCustomerOrders(1L, PageRequest.of(0, 10));

        assertEquals(1, responses.getTotalElements());
    }

    @Test
    void getOrdersByStatus_returnsPage() {
        Order order = Order.builder().id(2L).customer(customer).status(Order.OrderStatus.PENDING).totalAmount(new BigDecimal("5.00")).build();
        Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(orderRepository.findByStatus(eq(Order.OrderStatus.PENDING), any())).thenReturn(page);
        when(orderItemRepository.findByOrderId(2L)).thenReturn(List.of());

        Page<OrderResponse> responses = orderService.getOrdersByStatus(Order.OrderStatus.PENDING, PageRequest.of(0, 10));

        assertEquals(1, responses.getTotalElements());
    }
}
