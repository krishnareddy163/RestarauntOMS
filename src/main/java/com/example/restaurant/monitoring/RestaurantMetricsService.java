package com.example.restaurant.monitoring;

import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PaymentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.CountingMode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantMetricsService {
    private final MeterRegistry meterRegistry;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private Counter ordersCreatedCounter;
    private Counter paymentsProcessedCounter;
    private Counter deliveriesCompletedCounter;
    private Timer orderProcessingTimer;

    private final AtomicInteger activeOrders = new AtomicInteger(0);
    private final AtomicLong successfulPaymentsToday = new AtomicLong(0L);

    @PostConstruct
    public void initialize() {
        initializeMetrics();
    }

    private void initializeMetrics() {
        ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created")
                .register(meterRegistry);

        paymentsProcessedCounter = Counter.builder("payments.processed.total")
                .description("Total number of payments processed")
                .register(meterRegistry);

        deliveriesCompletedCounter = Counter.builder("deliveries.completed.total")
                .description("Total number of deliveries completed")
                .register(meterRegistry);

        orderProcessingTimer = Timer.builder("order.processing.time")
                .description("Time taken to process an order")
                .register(meterRegistry);

        // Gauge for active orders
        meterRegistry.gauge("orders.active", activeOrders, AtomicInteger::get);

        // Gauge for successful payments
        meterRegistry.gauge("payments.success.today", successfulPaymentsToday, AtomicLong::get);
    }

    public void recordOrderCreated() {
        ordersCreatedCounter.increment();
        updateActiveOrders();
        log.debug("Order created metric recorded");
    }

    public void recordPaymentProcessed() {
        paymentsProcessedCounter.increment();
        updateSuccessfulPayments();
        log.debug("Payment processed metric recorded");
    }

    public void recordDeliveryCompleted() {
        deliveriesCompletedCounter.increment();
        log.debug("Delivery completed metric recorded");
    }

    public void recordOrderProcessingTime(long durationMillis) {
        orderProcessingTimer.record(java.time.Duration.ofMillis(durationMillis));
        log.debug("Order processing time recorded: {} ms", durationMillis);
    }

    public void recordCustomMetric(String metricName, long value) {
        meterRegistry.counter(metricName).increment(value);
        log.debug("Custom metric recorded: {} = {}", metricName, value);
    }

    private void updateActiveOrders() {
        try {
            int count = orderRepository.findByStatus(com.example.restaurant.entity.Order.OrderStatus.PENDING).size();
            activeOrders.set(count);
        } catch (Exception e) {
            log.error("Error updating active orders gauge", e);
        }
    }

    private void updateSuccessfulPayments() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            long count = paymentRepository.countSuccessfulPaymentsByDateRange(startOfDay, endOfDay);
            successfulPaymentsToday.set(count);
        } catch (Exception e) {
            log.error("Error updating successful payments gauge", e);
        }
    }
}

