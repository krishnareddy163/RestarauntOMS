package com.example.restaurant.service;

import com.example.restaurant.dto.PaymentRequest;
import com.example.restaurant.dto.PaymentResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.Payment;
import com.example.restaurant.entity.User;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantEventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private User customer;

    @BeforeEach
    public void setUp() {
        customer = User.builder()
                .id(1L)
                .email("customer@test.com")
                .name("Test Customer")
                .role(User.UserRole.CUSTOMER)
                .active(true)
                .build();

        order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(Order.OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("50.00"))
                .build();
    }

    @Test
    public void testProcessPaymentSuccess() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(1L);
            }
            return payment;
        });
        
        // Mock event producer
        doNothing().when(eventProducer).publishPaymentProcessedEvent(any());

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals("SUCCESS", response.getStatus());
        verify(paymentRepository, atLeastOnce()).save(any());
        verify(eventProducer, times(1)).publishPaymentProcessedEvent(any());
    }

    @Test
    public void testProcessPaymentWithInvalidOrder() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId(999L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> paymentService.processPayment(request));
    }

    @Test
    public void testRefundPayment() {
        // Arrange
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .status(Payment.PaymentStatus.SUCCESS)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.refundPayment(1L);

        // Assert
        assertNotNull(response);
        assertEquals("REFUNDED", response.getStatus());
        verify(paymentRepository, times(1)).save(any());
    }
}
