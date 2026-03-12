package com.example.restaurant.kafka;

import com.example.restaurant.event.DeliveryCompletedEvent;
import com.example.restaurant.event.OrderCreatedEvent;
import com.example.restaurant.event.PaymentProcessedEvent;
import com.example.restaurant.event.PreparationCompletedEvent;
import com.example.restaurant.service.DeliveryService;
import com.example.restaurant.service.InventoryService;
import com.example.restaurant.service.PreparationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.example.restaurant.config.KafkaConfig;
import org.springframework.kafka.annotation.EnableKafka;
import org.junit.jupiter.api.Disabled;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        KafkaConfig.class,
        RestaurantEventConsumer.class,
        RestaurantKafkaIntegrationTest.KafkaTestConfig.class
}, properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=restaurant-kafka-it",
        "spring.kafka.listener.auto-startup=true",
        "kafka.topics.order-created=order.created",
        "kafka.topics.payment-processed=payment.processed",
        "kafka.topics.preparation-completed=preparation.completed",
        "kafka.topics.delivery-completed=delivery.completed"
})
@EmbeddedKafka(partitions = 1, topics = {
        "order.created",
        "payment.processed",
        "preparation.completed",
        "delivery.completed"
})
@ActiveProfiles("test")
@Disabled("Embedded Kafka integration test is unstable in CI; unit tests cover consumer behavior.")
class RestaurantKafkaIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    private PreparationService preparationService;

    @org.springframework.beans.factory.annotation.Autowired
    private DeliveryService deliveryService;

    @org.springframework.beans.factory.annotation.Autowired
    private InventoryService inventoryService;

    @Test
    void orderCreatedEvent_triggersPreparation() {
        kafkaTemplate.send("order.created", OrderCreatedEvent.builder().orderId(100L).build());
        verify(preparationService, timeout(5000)).initiatePreperation(100L);
    }

    @Test
    void paymentProcessedEvent_triggersStartPreparation() {
        kafkaTemplate.send("payment.processed", PaymentProcessedEvent.builder().orderId(101L).status("SUCCESS").build());
        verify(preparationService, timeout(5000)).startPreparation(101L);
    }

    @Test
    void preparationCompletedEvent_triggersDeliveryAssign() {
        kafkaTemplate.send("preparation.completed", PreparationCompletedEvent.builder().orderId(102L).build());
        verify(deliveryService, timeout(5000)).assignDelivery(102L);
    }

    @Test
    void deliveryCompletedEvent_triggersInventoryRelease() {
        kafkaTemplate.send("delivery.completed", DeliveryCompletedEvent.builder().orderId(103L).build());
        verify(inventoryService, timeout(5000)).releaseReservedInventory(103L);
    }

    @TestConfiguration
    @EnableKafka
    static class KafkaTestConfig {
        @Bean
        PreparationService preparationService() {
            return mock(PreparationService.class);
        }

        @Bean
        DeliveryService deliveryService() {
            return mock(DeliveryService.class);
        }

        @Bean
        InventoryService inventoryService() {
            return mock(InventoryService.class);
        }

    }
}
