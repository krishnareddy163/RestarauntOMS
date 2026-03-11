package com.example.restaurant.controller;

import com.example.restaurant.dto.PaymentRequest;
import com.example.restaurant.dto.PaymentResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final RestaurantMetricsService metricsService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());
        try {
            PaymentResponse response = paymentService.processPayment(request);
            metricsService.recordPaymentProcessed();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        log.info("Fetching payment: {}", paymentId);
        try {
            PaymentResponse response = paymentService.getPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Fetching payment for order: {}", orderId);
        try {
            PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        log.info("Refunding payment: {}", paymentId);
        try {
            PaymentResponse response = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error refunding payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

