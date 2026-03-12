package com.example.restaurant.kafka;

import com.example.restaurant.event.DeliveryCompletedEvent;
import com.example.restaurant.event.OrderCreatedEvent;
import com.example.restaurant.event.PaymentProcessedEvent;
import com.example.restaurant.event.PreparationCompletedEvent;
import com.example.restaurant.service.DeliveryService;
import com.example.restaurant.service.InventoryService;
import com.example.restaurant.service.PreparationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantEventConsumerTest {

    @Mock
    private PreparationService preparationService;

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private InventoryService inventoryService;

    private RestaurantEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new RestaurantEventConsumer(preparationService, deliveryService, inventoryService);
    }

    @Test
    void handleOrderCreatedEvent_initiatesPreparation() {
        consumer.handleOrderCreatedEvent(OrderCreatedEvent.builder().orderId(1L).build());
        verify(preparationService).initiatePreperation(1L);
    }

    @Test
    void handlePaymentProcessedEvent_success_startsPreparation() {
        consumer.handlePaymentProcessedEvent(PaymentProcessedEvent.builder().orderId(2L).status("SUCCESS").build());
        verify(preparationService).startPreparation(2L);
    }

    @Test
    void handlePaymentProcessedEvent_nonSuccess_noStart() {
        consumer.handlePaymentProcessedEvent(PaymentProcessedEvent.builder().orderId(3L).status("FAILED").build());
        verify(preparationService, never()).startPreparation(3L);
    }

    @Test
    void handlePreparationCompletedEvent_assignsDelivery() {
        consumer.handlePreparationCompletedEvent(PreparationCompletedEvent.builder().orderId(4L).build());
        verify(deliveryService).assignDelivery(4L);
    }

    @Test
    void handleDeliveryCompletedEvent_releasesInventory() {
        consumer.handleDeliveryCompletedEvent(DeliveryCompletedEvent.builder().orderId(5L).build());
        verify(inventoryService).releaseReservedInventory(5L);
    }

    @Test
    void handleOrderCreatedEvent_exception_isHandled() {
        doThrow(new RuntimeException("fail")).when(preparationService).initiatePreperation(10L);
        consumer.handleOrderCreatedEvent(OrderCreatedEvent.builder().orderId(10L).build());
        verify(preparationService).initiatePreperation(10L);
    }

    @Test
    void handlePaymentProcessedEvent_exception_isHandled() {
        doThrow(new RuntimeException("fail")).when(preparationService).startPreparation(11L);
        consumer.handlePaymentProcessedEvent(PaymentProcessedEvent.builder().orderId(11L).status("SUCCESS").build());
        verify(preparationService).startPreparation(11L);
    }

    @Test
    void handlePreparationCompletedEvent_exception_isHandled() {
        doThrow(new RuntimeException("fail")).when(deliveryService).assignDelivery(12L);
        consumer.handlePreparationCompletedEvent(PreparationCompletedEvent.builder().orderId(12L).build());
        verify(deliveryService).assignDelivery(12L);
    }

    @Test
    void handleDeliveryCompletedEvent_exception_isHandled() {
        doThrow(new RuntimeException("fail")).when(inventoryService).releaseReservedInventory(13L);
        consumer.handleDeliveryCompletedEvent(DeliveryCompletedEvent.builder().orderId(13L).build());
        verify(inventoryService).releaseReservedInventory(13L);
    }
}
