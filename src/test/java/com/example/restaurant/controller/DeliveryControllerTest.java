package com.example.restaurant.controller;

import com.example.restaurant.dto.DeliveryResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;
    @Mock
    private RestaurantMetricsService metricsService;

    @InjectMocks
    private DeliveryController deliveryController;

    @Test
    void assignDelivery_success() {
        DeliveryResponse response = DeliveryResponse.builder().id(1L).orderId(10L).status("ASSIGNED").build();
        when(deliveryService.assignDelivery(10L)).thenReturn(response);

        ResponseEntity<DeliveryResponse> res = deliveryController.assignDelivery(10L);

        assertEquals(201, res.getStatusCode().value());
        assertEquals(1L, res.getBody().getId());
    }

    @Test
    void assignDelivery_failure() {
        when(deliveryService.assignDelivery(10L)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<DeliveryResponse> res = deliveryController.assignDelivery(10L);

        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void pickupOrder_success() {
        DeliveryResponse response = DeliveryResponse.builder().id(2L).status("PICKED_UP").build();
        when(deliveryService.pickupOrder(2L)).thenReturn(response);

        ResponseEntity<DeliveryResponse> res = deliveryController.pickupOrder(2L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals("PICKED_UP", res.getBody().getStatus());
    }

    @Test
    void pickupOrder_failure() {
        when(deliveryService.pickupOrder(2L)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<DeliveryResponse> res = deliveryController.pickupOrder(2L);

        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void updateLocation_success() {
        DeliveryResponse response = DeliveryResponse.builder().id(3L).status("IN_TRANSIT").build();
        when(deliveryService.updateDeliveryLocation(3L, "1", "2")).thenReturn(response);

        ResponseEntity<DeliveryResponse> res = deliveryController.updateLocation(3L, "1", "2");

        assertEquals(200, res.getStatusCode().value());
        assertEquals("IN_TRANSIT", res.getBody().getStatus());
    }

    @Test
    void updateLocation_failure() {
        when(deliveryService.updateDeliveryLocation(3L, "1", "2")).thenThrow(new RuntimeException("fail"));

        ResponseEntity<DeliveryResponse> res = deliveryController.updateLocation(3L, "1", "2");

        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void completeDelivery_success() {
        DeliveryResponse response = DeliveryResponse.builder().id(4L).status("COMPLETED").build();
        when(deliveryService.completeDelivery(4L)).thenReturn(response);

        ResponseEntity<DeliveryResponse> res = deliveryController.completeDelivery(4L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals("COMPLETED", res.getBody().getStatus());
        verify(metricsService, times(1)).recordDeliveryCompleted();
    }

    @Test
    void completeDelivery_failure() {
        when(deliveryService.completeDelivery(4L)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<DeliveryResponse> res = deliveryController.completeDelivery(4L);

        assertEquals(400, res.getStatusCode().value());
    }

    @Test
    void getDriverDeliveries_success() {
        when(deliveryService.getDriverDeliveries(1L)).thenReturn(List.of(DeliveryResponse.builder().id(5L).build()));

        ResponseEntity<List<DeliveryResponse>> res = deliveryController.getDriverDeliveries(1L);

        assertEquals(200, res.getStatusCode().value());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getDriverDeliveries_failure() {
        when(deliveryService.getDriverDeliveries(1L)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<List<DeliveryResponse>> res = deliveryController.getDriverDeliveries(1L);

        assertEquals(404, res.getStatusCode().value());
    }
}
