package com.example.restaurant.service;

import com.example.restaurant.dto.DeliveryResponse;
import com.example.restaurant.entity.Delivery;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.User;
import com.example.restaurant.event.DeliveryCompletedEvent;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.DeliveryRepository;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantEventProducer eventProducer;

    @InjectMocks
    private DeliveryService deliveryService;

    private Order order;
    private User driver;

    @BeforeEach
    void setUp() {
        order = Order.builder().id(1L).status(Order.OrderStatus.CONFIRMED).build();
        driver = User.builder().id(3L).role(User.UserRole.DRIVER).active(true).build();
    }

    @Test
    void assignDelivery_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findActiveUsersByRole(User.UserRole.DRIVER)).thenReturn(List.of(driver));
        when(deliveryRepository.save(any())).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DeliveryResponse response = deliveryService.assignDelivery(1L);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getOrderId());
    }

    @Test
    void assignDelivery_noDriver_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findActiveUsersByRole(User.UserRole.DRIVER)).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> deliveryService.assignDelivery(1L));
    }

    @Test
    void pickupOrder_success() {
        Delivery delivery = Delivery.builder().id(5L).order(order).status(Delivery.DeliveryStatus.ASSIGNED).build();
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any())).thenReturn(delivery);
        when(orderRepository.save(any())).thenReturn(order);

        DeliveryResponse response = deliveryService.pickupOrder(5L);

        assertEquals("PICKED_UP", response.getStatus());
        assertEquals(Order.OrderStatus.PICKED_UP, order.getStatus());
    }

    @Test
    void pickupOrder_missing_throws() {
        when(deliveryRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> deliveryService.pickupOrder(5L));
    }

    @Test
    void updateDeliveryLocation_success() {
        Delivery delivery = Delivery.builder().id(6L).order(order).status(Delivery.DeliveryStatus.ASSIGNED).build();
        when(deliveryRepository.findById(6L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any())).thenReturn(delivery);

        DeliveryResponse response = deliveryService.updateDeliveryLocation(6L, "12.34", "56.78");

        assertEquals("IN_TRANSIT", response.getStatus());
        assertEquals(new BigDecimal("12.34"), response.getCurrentLatitude());
    }

    @Test
    void updateDeliveryLocation_missing_throws() {
        when(deliveryRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> deliveryService.updateDeliveryLocation(6L, "1", "2"));
    }

    @Test
    void completeDelivery_success() {
        Delivery delivery = Delivery.builder().id(7L).order(order).status(Delivery.DeliveryStatus.IN_TRANSIT).build();
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any())).thenReturn(delivery);
        when(orderRepository.save(any())).thenReturn(order);

        DeliveryResponse response = deliveryService.completeDelivery(7L);

        assertEquals("COMPLETED", response.getStatus());
        verify(eventProducer, times(1)).publishDeliveryCompletedEvent(any(DeliveryCompletedEvent.class));
        assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getCompletedAt());
        assertNotNull(delivery.getDeliveryTime());
    }

    @Test
    void completeDelivery_missing_throws() {
        when(deliveryRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> deliveryService.completeDelivery(7L));
    }

    @Test
    void getDriverDeliveries_success() {
        Delivery delivery = Delivery.builder().id(8L).order(order).driver(driver).status(Delivery.DeliveryStatus.ASSIGNED).build();
        when(deliveryRepository.findActiveDeliveriesByDriver(eq(3L), any())).thenReturn(List.of(delivery));

        List<DeliveryResponse> responses = deliveryService.getDriverDeliveries(3L);

        assertEquals(1, responses.size());
        assertEquals(3L, responses.get(0).getDriverId());
    }

    @Test
    void getDriverDeliveries_failure() {
        when(deliveryRepository.findActiveDeliveriesByDriver(eq(3L), any())).thenThrow(new RuntimeException("fail"));
        assertThrows(RuntimeException.class, () -> deliveryService.getDriverDeliveries(3L));
    }
}
