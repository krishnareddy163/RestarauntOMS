package com.example.restaurant.controller;

import com.example.restaurant.dto.DeliveryResponse;
import com.example.restaurant.monitoring.RestaurantMetricsService;
import com.example.restaurant.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final RestaurantMetricsService metricsService;

    @PostMapping("/{orderId}/assign")
    public ResponseEntity<DeliveryResponse> assignDelivery(@PathVariable Long orderId) {
        log.info("Assigning delivery for order: {}", orderId);
        try {
            DeliveryResponse response = deliveryService.assignDelivery(orderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error assigning delivery", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{deliveryId}/pickup")
    public ResponseEntity<DeliveryResponse> pickupOrder(@PathVariable Long deliveryId) {
        log.info("Picking up order for delivery: {}", deliveryId);
        try {
            DeliveryResponse response = deliveryService.pickupOrder(deliveryId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error picking up delivery", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{deliveryId}/location")
    public ResponseEntity<DeliveryResponse> updateLocation(
            @PathVariable Long deliveryId,
            @RequestParam String latitude,
            @RequestParam String longitude) {
        log.info("Updating delivery location for delivery: {}", deliveryId);
        try {
            DeliveryResponse response = deliveryService.updateDeliveryLocation(deliveryId, latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating delivery location", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{deliveryId}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long deliveryId) {
        log.info("Completing delivery: {}", deliveryId);
        try {
            DeliveryResponse response = deliveryService.completeDelivery(deliveryId);
            metricsService.recordDeliveryCompleted();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error completing delivery", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponse>> getDriverDeliveries(@PathVariable Long driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        try {
            List<DeliveryResponse> response = deliveryService.getDriverDeliveries(driverId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching driver deliveries", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

