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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
}
