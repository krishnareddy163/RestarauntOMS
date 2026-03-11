package com.example.restaurant.controller;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RestaurantMetricsService metricsService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        long startTime = System.currentTimeMillis();
        try {
            OrderResponse response = orderService.createOrder(request);
            metricsService.recordOrderCreated();
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordOrderProcessingTime(duration);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        log.info("Fetching order: {}", orderId);
        try {
            OrderResponse response = orderService.getOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching order", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(
            @PathVariable Long customerId,
            Pageable pageable) {
        log.info("Fetching orders for customer: {}", customerId);
        try {
            Page<OrderResponse> response = orderService.getCustomerOrders(customerId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching customer orders", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        log.info("Updating order {} status to: {}", orderId, status);
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
