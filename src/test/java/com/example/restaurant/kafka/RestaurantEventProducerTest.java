package com.example.restaurant.kafka;

import com.example.restaurant.event.DeliveryCompletedEvent;
import com.example.restaurant.event.OrderCreatedEvent;
import com.example.restaurant.event.PaymentProcessedEvent;
import com.example.restaurant.event.PreparationCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private RestaurantEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new RestaurantEventProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "orderCreatedTopic", "order.created");
        ReflectionTestUtils.setField(producer, "paymentProcessedTopic", "payment.processed");
        ReflectionTestUtils.setField(producer, "preparationCompletedTopic", "preparation.completed");
        ReflectionTestUtils.setField(producer, "deliveryCompletedTopic", "delivery.completed");
        lenient().when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void publishOrderCreatedEvent_sendsMessageWithTopicAndKey() {
        OrderCreatedEvent event = OrderCreatedEvent.builder().orderId(10L).build();

        producer.publishOrderCreatedEvent(event);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());
        Message msg = captor.getValue();
        assertEquals("order.created", msg.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("10", msg.getHeaders().get("kafka_messageKey"));
        assertEquals(event, msg.getPayload());
    }

    @Test
    void publishPaymentProcessedEvent_sendsMessageWithTopicAndKey() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder().orderId(11L).build();

        producer.publishPaymentProcessedEvent(event);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());
        Message msg = captor.getValue();
        assertEquals("payment.processed", msg.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("11", msg.getHeaders().get("kafka_messageKey"));
        assertEquals(event, msg.getPayload());
    }

    @Test
    void publishPreparationCompletedEvent_sendsMessageWithTopicAndKey() {
        PreparationCompletedEvent event = PreparationCompletedEvent.builder().orderId(12L).build();

        producer.publishPreparationCompletedEvent(event);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());
        Message msg = captor.getValue();
        assertEquals("preparation.completed", msg.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("12", msg.getHeaders().get("kafka_messageKey"));
        assertEquals(event, msg.getPayload());
    }

    @Test
    void publishDeliveryCompletedEvent_sendsMessageWithTopicAndKey() {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder().orderId(13L).build();

        producer.publishDeliveryCompletedEvent(event);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());
        Message msg = captor.getValue();
        assertEquals("delivery.completed", msg.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("13", msg.getHeaders().get("kafka_messageKey"));
        assertEquals(event, msg.getPayload());
    }

    @Test
    void publishOrderCreatedEvent_nullEvent_throws() {
        assertThrows(IllegalArgumentException.class, () -> producer.publishOrderCreatedEvent(null));
    }

    @Test
    void publishPaymentProcessedEvent_nullEvent_throws() {
        assertThrows(IllegalArgumentException.class, () -> producer.publishPaymentProcessedEvent(null));
    }

    @Test
    void publishPreparationCompletedEvent_nullEvent_throws() {
        assertThrows(IllegalArgumentException.class, () -> producer.publishPreparationCompletedEvent(null));
    }

    @Test
    void publishDeliveryCompletedEvent_nullEvent_throws() {
        assertThrows(IllegalArgumentException.class, () -> producer.publishDeliveryCompletedEvent(null));
    }

    @Test
    void publishOrderCreatedEvent_nullOrderId_throws() {
        OrderCreatedEvent event = OrderCreatedEvent.builder().orderId(null).build();
        assertThrows(IllegalArgumentException.class, () -> producer.publishOrderCreatedEvent(event));
    }

    @Test
    void publishPaymentProcessedEvent_nullOrderId_throws() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder().orderId(null).build();
        assertThrows(IllegalArgumentException.class, () -> producer.publishPaymentProcessedEvent(event));
    }

    @Test
    void publishPreparationCompletedEvent_nullOrderId_throws() {
        PreparationCompletedEvent event = PreparationCompletedEvent.builder().orderId(null).build();
        assertThrows(IllegalArgumentException.class, () -> producer.publishPreparationCompletedEvent(event));
    }

    @Test
    void publishDeliveryCompletedEvent_nullOrderId_throws() {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder().orderId(null).build();
        assertThrows(IllegalArgumentException.class, () -> producer.publishDeliveryCompletedEvent(event));
    }

    @Test
    void publishOrderCreatedEvent_sendThrows_wrapsRuntimeException() {
        OrderCreatedEvent event = OrderCreatedEvent.builder().orderId(20L).build();
        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> producer.publishOrderCreatedEvent(event));
    }

    @Test
    void publishPaymentProcessedEvent_sendThrows_wrapsRuntimeException() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder().orderId(21L).build();
        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> producer.publishPaymentProcessedEvent(event));
    }

    @Test
    void publishPreparationCompletedEvent_sendThrows_wrapsRuntimeException() {
        PreparationCompletedEvent event = PreparationCompletedEvent.builder().orderId(22L).build();
        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> producer.publishPreparationCompletedEvent(event));
    }

    @Test
    void publishDeliveryCompletedEvent_sendThrows_wrapsRuntimeException() {
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder().orderId(23L).build();
        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> producer.publishDeliveryCompletedEvent(event));
    }
}
