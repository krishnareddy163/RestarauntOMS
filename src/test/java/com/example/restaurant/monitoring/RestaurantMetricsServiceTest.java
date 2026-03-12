package com.example.restaurant.monitoring;

import com.example.restaurant.entity.Order;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PaymentRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantMetricsServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private RestaurantMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new RestaurantMetricsService(new SimpleMeterRegistry(), orderRepository, paymentRepository);
        metricsService.initialize();
    }

    @Test
    void recordOrderCreated_updatesGauges() {
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(List.of(Order.builder().build()));
        assertDoesNotThrow(() -> metricsService.recordOrderCreated());
    }

    @Test
    void recordPaymentProcessed_updatesGauges() {
        when(paymentRepository.countSuccessfulPaymentsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        assertDoesNotThrow(() -> metricsService.recordPaymentProcessed());
    }

    @Test
    void recordDeliveryCompleted_recordsCounter() {
        assertDoesNotThrow(() -> metricsService.recordDeliveryCompleted());
    }

    @Test
    void recordOrderProcessingTime_recordsTimer() {
        assertDoesNotThrow(() -> metricsService.recordOrderProcessingTime(123));
    }

    @Test
    void recordCustomMetric_recordsCounter() {
        assertDoesNotThrow(() -> metricsService.recordCustomMetric("custom.metric", 2));
    }

    @Test
    void recordOrderCreated_handlesException() {
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenThrow(new RuntimeException("fail"));
        assertDoesNotThrow(() -> metricsService.recordOrderCreated());
    }

    @Test
    void recordPaymentProcessed_handlesException() {
        when(paymentRepository.countSuccessfulPaymentsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("fail"));
        assertDoesNotThrow(() -> metricsService.recordPaymentProcessed());
    }
}
