package com.example.restaurant.service;

import com.example.restaurant.dto.PaymentRequest;
import com.example.restaurant.dto.PaymentResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.Payment;
import com.example.restaurant.event.PaymentProcessedEvent;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestaurantEventProducer eventProducer;

    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Create payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(request.getAmount())
                    .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
                    .status(Payment.PaymentStatus.PENDING)
                    .transactionId(UUID.randomUUID().toString())
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Payment processing started for order {}: transactionId={}", order.getId(), payment.getTransactionId());

            // Simulate payment gateway integration
            simulatePaymentGateway(payment);

            // Publish event
            eventProducer.publishPaymentProcessedEvent(PaymentProcessedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(order.getId())
                    .amount(request.getAmount().toString())
                    .status(payment.getStatus().toString())
                    .processedAt(payment.getUpdatedAt().toString())
                    .build());

            log.info("Payment {} processed successfully", payment.getId());
            return convertToResponse(payment);
        } catch (Exception e) {
            log.error("Error processing payment for order {}", request.getOrderId(), e);
            throw new RuntimeException("Failed to process payment", e);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));
        return convertToResponse(payment);
    }

    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getStatus().equals(Payment.PaymentStatus.SUCCESS)) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment {} refunded", paymentId);
        return convertToResponse(payment);
    }

    private void simulatePaymentGateway(Payment payment) {
        // Simulate payment gateway call
        try {
            Thread.sleep(500); // Simulate gateway latency
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.error("Payment gateway simulation failed", e);
        }
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().toString())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
