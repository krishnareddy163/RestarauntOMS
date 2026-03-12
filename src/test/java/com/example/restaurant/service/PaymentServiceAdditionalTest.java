package com.example.restaurant.service;

import com.example.restaurant.dto.PaymentRequest;
import com.example.restaurant.dto.PaymentResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.Payment;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceAdditionalTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantEventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder().id(1L).build();
    }

    @Test
    void processPayment_orderNotFound_throws() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(new BigDecimal("9.99"));
        request.setPaymentMethod("CREDIT_CARD");

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void refundPayment_success() {
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(new BigDecimal("9.99"))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId("tx")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        PaymentResponse response = paymentService.refundPayment(1L);

        assertEquals("REFUNDED", response.getStatus());
    }

    @Test
    void refundPayment_notSuccess_throws() {
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(new BigDecimal("9.99"))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .status(Payment.PaymentStatus.FAILED)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> paymentService.refundPayment(1L));
    }

    @Test
    void processPayment_interrupted_setsFailed() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(new BigDecimal("9.99"));
        request.setPaymentMethod("CASH");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Thread.currentThread().interrupt();
        try {
            PaymentResponse response = paymentService.processPayment(request);
            assertEquals("FAILED", response.getStatus());
        } finally {
            Thread.interrupted();
        }
    }
}
