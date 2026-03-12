package com.example.restaurant.kafka;

import com.example.restaurant.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.payment-processed}")
    private String paymentProcessedTopic;

    @Value("${kafka.topics.preparation-completed}")
    private String preparationCompletedTopic;

    @Value("${kafka.topics.delivery-completed}")
    private String deliveryCompletedTopic;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            if (event == null || event.getOrderId() == null) {
                log.error("Cannot publish order created event: null event or order ID");
                throw new IllegalArgumentException("Order created event or order ID cannot be null");
            }
            
            String messageKey = event.getOrderId().toString();
            Message<OrderCreatedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, orderCreatedTopic)
                    .setHeader("kafka_messageKey", messageKey)
                    .build();
            
            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish order created event for order ID: {}", event.getOrderId(), ex);
                } else {
                    log.info("Order created event published successfully for order ID: {}", event.getOrderId());
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Invalid order created event data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error publishing order created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish order created event", e);
        }
    }

    public void publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        try {
            if (event == null || event.getOrderId() == null) {
                log.error("Cannot publish payment processed event: null event or order ID");
                throw new IllegalArgumentException("Payment processed event or order ID cannot be null");
            }
            
            String messageKey = event.getOrderId().toString();
            Message<PaymentProcessedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, paymentProcessedTopic)
                    .setHeader("kafka_messageKey", messageKey)
                    .build();
            
            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish payment processed event for order ID: {}", event.getOrderId(), ex);
                } else {
                    log.info("Payment processed event published successfully for order ID: {}", event.getOrderId());
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment processed event data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error publishing payment processed event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish payment processed event", e);
        }
    }

    public void publishPreparationCompletedEvent(PreparationCompletedEvent event) {
        try {
            if (event == null || event.getOrderId() == null) {
                log.error("Cannot publish preparation completed event: null event or order ID");
                throw new IllegalArgumentException("Preparation completed event or order ID cannot be null");
            }
            
            String messageKey = event.getOrderId().toString();
            Message<PreparationCompletedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, preparationCompletedTopic)
                    .setHeader("kafka_messageKey", messageKey)
                    .build();
            
            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish preparation completed event for order ID: {}", event.getOrderId(), ex);
                } else {
                    log.info("Preparation completed event published successfully for order ID: {}", event.getOrderId());
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Invalid preparation completed event data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error publishing preparation completed event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish preparation completed event", e);
        }
    }

    public void publishDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        try {
            if (event == null || event.getOrderId() == null) {
                log.error("Cannot publish delivery completed event: null event or order ID");
                throw new IllegalArgumentException("Delivery completed event or order ID cannot be null");
            }
            
            String messageKey = event.getOrderId().toString();
            Message<DeliveryCompletedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, deliveryCompletedTopic)
                    .setHeader("kafka_messageKey", messageKey)
                    .build();
            
            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish delivery completed event for order ID: {}", event.getOrderId(), ex);
                } else {
                    log.info("Delivery completed event published successfully for order ID: {}", event.getOrderId());
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Invalid delivery completed event data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error publishing delivery completed event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish delivery completed event", e);
        }
    }
}
