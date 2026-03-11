package com.example.restaurant.service;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.User;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.OrderItemRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

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
    public void setUp() {
        customer = User.builder()
                .id(1L)
                .email("customer@test.com")
                .name("Test Customer")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .build();

        menuItem = MenuItem.builder()
                .id(1L)
                .name("Burger")
                .price(new BigDecimal("5.99"))
                .available(true)
                .category("Main Course")
                .preparationTimeMinutes(10)
                .build();
    }

    @Test
    public void testCreateOrderSuccess() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .deliveryAddress("123 Main St")
                .deliveryType("DELIVERY")
                .items(java.util.List.of(
                        CreateOrderRequest.OrderItemRequest.builder()
                                .menuItemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(1L);
            }
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(java.time.LocalDateTime.now());
            }
            order.setUpdatedAt(java.time.LocalDateTime.now());
            return order;
        });
        
        doNothing().when(inventoryService).reserveInventory(any());

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCustomerId());
        assertEquals("CONFIRMED", response.getStatus());
        verify(orderRepository, times(2)).save(any());
        verify(eventProducer, times(1)).publishOrderCreatedEvent(any());
    }

    @Test
    public void testCreateOrderWithInvalidCustomer() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(999L)
                .deliveryAddress("123 Main St")
                .deliveryType("DELIVERY")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    public void testGetOrder() {
        // Arrange
        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(Order.OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("11.98"))
                .deliveryType(Order.DeliveryType.DELIVERY)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(java.util.List.of(
                com.example.restaurant.entity.OrderItem.builder()
                        .id(1L)
                        .order(order)
                        .menuItem(menuItem)
                        .quantity(2)
                        .unitPrice(menuItem.getPrice())
                        .build()
        ));

        // Act
        OrderResponse response = orderService.getOrder(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CONFIRMED", response.getStatus());
    }

    @Test
    public void testUpdateOrderStatus() {
        // Arrange
        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        // Act
        orderService.updateOrderStatus(1L, "PREPARING");

        // Assert
        assertEquals(Order.OrderStatus.PREPARING, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
