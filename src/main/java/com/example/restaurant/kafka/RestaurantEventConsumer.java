package com.example.restaurant.kafka;

import com.example.restaurant.event.*;
import com.example.restaurant.service.DeliveryService;
import com.example.restaurant.service.InventoryService;
import com.example.restaurant.service.PreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantEventConsumer {
    private final PreparationService preparationService;
    private final DeliveryService deliveryService;
    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "${kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCreatedListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("Received order created event for order ID: {}", event.getOrderId());
            preparationService.initiatePreperation(event.getOrderId());
            log.info("Preparation initiated for order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error handling order created event for order ID: {}", event.getOrderId(), e);
            // Implement retry logic here if needed
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.payment-processed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentProcessedListenerContainerFactory"
    )
    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        try {
            log.info("Received payment processed event for order ID: {}", event.getOrderId());
            if ("SUCCESS".equals(event.getStatus())) {
                preparationService.startPreparation(event.getOrderId());
                log.info("Preparation started for order ID: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error handling payment processed event for order ID: {}", event.getOrderId(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.preparation-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "preparationCompletedListenerContainerFactory"
    )
    public void handlePreparationCompletedEvent(PreparationCompletedEvent event) {
        try {
            log.info("Received preparation completed event for order ID: {}", event.getOrderId());
            deliveryService.assignDelivery(event.getOrderId());
            log.info("Delivery assigned for order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error handling preparation completed event for order ID: {}", event.getOrderId(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.delivery-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "deliveryCompletedListenerContainerFactory"
    )
    public void handleDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        try {
            log.info("Received delivery completed event for order ID: {}", event.getOrderId());
            // Update order status and release reserved inventory
            inventoryService.releaseReservedInventory(event.getOrderId());
            log.info("Inventory released for order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error handling delivery completed event for order ID: {}", event.getOrderId(), e);
        }
    }
}
