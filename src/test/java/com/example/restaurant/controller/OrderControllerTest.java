package com.example.restaurant.controller;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private RestaurantMetricsService metricsService;

    @InjectMocks
    private OrderController orderController;

    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    public void setUp() {
        createOrderRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .deliveryAddress("123 Main St")
                .deliveryType("DELIVERY")
                .items(List.of(
                        CreateOrderRequest.OrderItemRequest.builder()
                                .menuItemId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();
    }

    @Test
    public void testCreateOrderSuccess() {
        // Arrange
        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("11.98"))
                .deliveryAddress("123 Main St")
                .deliveryType("DELIVERY")
                .items(List.of())
                .build();

        when(orderService.createOrder(any())).thenReturn(orderResponse);

        // Act
        ResponseEntity<OrderResponse> response = orderController.createOrder(createOrderRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(metricsService, times(1)).recordOrderCreated();
    }

    @Test
    public void testCreateOrderFailure() {
        // Arrange
        when(orderService.createOrder(any())).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<OrderResponse> response = orderController.createOrder(createOrderRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetOrder() {
        // Arrange
        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("11.98"))
                .build();

        when(orderService.getOrder(1L)).thenReturn(orderResponse);

        // Act
        ResponseEntity<OrderResponse> response = orderController.getOrder(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    public void testUpdateOrderStatus() {
        // Arrange
        doNothing().when(orderService).updateOrderStatus(1L, "PREPARING");

        // Act
        ResponseEntity<Void> response = orderController.updateOrderStatus(1L, "PREPARING");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).updateOrderStatus(1L, "PREPARING");
    }
}

