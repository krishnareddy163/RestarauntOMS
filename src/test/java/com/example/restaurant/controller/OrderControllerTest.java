package com.example.restaurant.controller;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        verify(metricsService, times(1)).recordOrderProcessingTime(anyLong());
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
    public void testGetOrderNotFound() {
        when(orderService.getOrder(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<OrderResponse> response = orderController.getOrder(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetCustomerOrders() {
        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("11.98"))
                .build();
        Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse), PageRequest.of(0, 10), 1);
        when(orderService.getCustomerOrders(eq(1L), any())).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response = orderController.getCustomerOrders(1L, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    public void testGetCustomerOrdersNotFound() {
        when(orderService.getCustomerOrders(eq(1L), any())).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<Page<OrderResponse>> response = orderController.getCustomerOrders(1L, PageRequest.of(0, 10));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
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

    @Test
    public void testUpdateOrderStatusFailure() {
        doThrow(new RuntimeException("bad status")).when(orderService).updateOrderStatus(1L, "BAD");

        ResponseEntity<Void> response = orderController.updateOrderStatus(1L, "BAD");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
