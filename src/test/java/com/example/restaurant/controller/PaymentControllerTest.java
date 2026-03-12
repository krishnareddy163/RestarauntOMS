package com.example.restaurant.controller;

import com.example.restaurant.dto.PaymentRequest;
import com.example.restaurant.dto.PaymentResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private RestaurantMetricsService metricsService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentRequest request;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(new BigDecimal("9.99"));
        request.setPaymentMethod("CREDIT_CARD");

        response = PaymentResponse.builder()
                .id(10L)
                .orderId(1L)
                .amount(new BigDecimal("9.99"))
                .paymentMethod("CREDIT_CARD")
                .status("SUCCESS")
                .build();
    }

    @Test
    void processPayment_success() {
        when(paymentService.processPayment(any())).thenReturn(response);

        ResponseEntity<PaymentResponse> res = paymentController.processPayment(request);

        assertEquals(201, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertEquals(10L, res.getBody().getId());
        verify(metricsService, times(1)).recordPaymentProcessed();
    }

    @Test
    void processPayment_failure() {
        when(paymentService.processPayment(any())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<PaymentResponse> res = paymentController.processPayment(request);

        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void getPayment_success() {
        when(paymentService.getPayment(10L)).thenReturn(response);

        ResponseEntity<PaymentResponse> res = paymentController.getPayment(10L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals(10L, res.getBody().getId());
    }

    @Test
    void getPayment_failure() {
        when(paymentService.getPayment(10L)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<PaymentResponse> res = paymentController.getPayment(10L);

        assertEquals(404, res.getStatusCode().value());
    }

    @Test
    void getPaymentByOrderId_success() {
        when(paymentService.getPaymentByOrderId(1L)).thenReturn(response);

        ResponseEntity<PaymentResponse> res = paymentController.getPaymentByOrderId(1L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals(1L, res.getBody().getOrderId());
    }

    @Test
    void getPaymentByOrderId_failure() {
        when(paymentService.getPaymentByOrderId(1L)).thenThrow(new RuntimeException("not found"));

        ResponseEntity<PaymentResponse> res = paymentController.getPaymentByOrderId(1L);

        assertEquals(404, res.getStatusCode().value());
    }

    @Test
    void refundPayment_success() {
        when(paymentService.refundPayment(10L)).thenReturn(response);

        ResponseEntity<PaymentResponse> res = paymentController.refundPayment(10L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals(10L, res.getBody().getId());
    }

    @Test
    void refundPayment_failure() {
        when(paymentService.refundPayment(10L)).thenThrow(new RuntimeException("bad"));

        ResponseEntity<PaymentResponse> res = paymentController.refundPayment(10L);

        assertEquals(400, res.getStatusCode().value());
    }
}
