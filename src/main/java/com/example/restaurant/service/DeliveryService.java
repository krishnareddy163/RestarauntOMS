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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantEventProducer eventProducer;

    public DeliveryResponse assignDelivery(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Find available driver
            User driver = findAvailableDriver();
            if (driver == null) {
                throw new RuntimeException("No available drivers");
            }

            Delivery delivery = Delivery.builder()
                    .order(order)
                    .driver(driver)
                    .status(Delivery.DeliveryStatus.ASSIGNED)
                    .build();

            delivery = deliveryRepository.save(delivery);
            log.info("Delivery assigned for order {} to driver {}", orderId, driver.getId());
            return convertToResponse(delivery);
        } catch (Exception e) {
            log.error("Error assigning delivery for order {}", orderId, e);
            throw new RuntimeException("Failed to assign delivery", e);
        }
    }

    public DeliveryResponse pickupOrder(Long deliveryId) {
        try {
            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            delivery.setStatus(Delivery.DeliveryStatus.PICKED_UP);
            delivery.setPickupTime(LocalDateTime.now());
            delivery = deliveryRepository.save(delivery);

            Order order = delivery.getOrder();
            order.setStatus(Order.OrderStatus.PICKED_UP);
            orderRepository.save(order);

            log.info("Order picked up for delivery {}", deliveryId);
            return convertToResponse(delivery);
        } catch (Exception e) {
            log.error("Error picking up delivery {}", deliveryId, e);
            throw new RuntimeException("Failed to pickup delivery", e);
        }
    }

    public DeliveryResponse updateDeliveryLocation(Long deliveryId, String latitude, String longitude) {
        try {
            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            delivery.setCurrentLatitude(new BigDecimal(latitude));
            delivery.setCurrentLongitude(new BigDecimal(longitude));
            delivery.setStatus(Delivery.DeliveryStatus.IN_TRANSIT);
            delivery = deliveryRepository.save(delivery);
            log.info("Delivery location updated for delivery {}: lat={}, lon={}", deliveryId, latitude, longitude);
            return convertToResponse(delivery);
        } catch (Exception e) {
            log.error("Error updating delivery location {}", deliveryId, e);
            throw new RuntimeException("Failed to update delivery location", e);
        }
    }

    public DeliveryResponse completeDelivery(Long deliveryId) {
        try {
            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            delivery.setStatus(Delivery.DeliveryStatus.COMPLETED);
            delivery.setDeliveryTime(LocalDateTime.now());
            delivery = deliveryRepository.save(delivery);

            // Update order status
            Order order = delivery.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);
            order.setCompletedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Publish event
            Long driverId = delivery.getDriver() != null ? delivery.getDriver().getId() : null;
            eventProducer.publishDeliveryCompletedEvent(DeliveryCompletedEvent.builder()
                    .deliveryId(delivery.getId())
                    .orderId(order.getId())
                    .driverId(driverId)
                    .status("COMPLETED")
                    .deliveredAt(delivery.getDeliveryTime().toString())
                    .build());

            log.info("Delivery completed for delivery {}", deliveryId);
            return convertToResponse(delivery);
        } catch (Exception e) {
            log.error("Error completing delivery {}", deliveryId, e);
            throw new RuntimeException("Failed to complete delivery", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDriverDeliveries(Long driverId) {
        try {
            List<Delivery.DeliveryStatus> activeStatuses = List.of(
                    Delivery.DeliveryStatus.ASSIGNED,
                    Delivery.DeliveryStatus.PICKED_UP,
                    Delivery.DeliveryStatus.IN_TRANSIT
            );
            return deliveryRepository.findActiveDeliveriesByDriver(driverId, activeStatuses)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching deliveries for driver {}", driverId, e);
            throw new RuntimeException("Failed to fetch driver deliveries", e);
        }
    }

    private User findAvailableDriver() {
        return userRepository.findActiveUsersByRole(User.UserRole.DRIVER)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private DeliveryResponse convertToResponse(Delivery delivery) {
        Long orderId = delivery.getOrder() != null ? delivery.getOrder().getId() : null;
        Long driverId = delivery.getDriver() != null ? delivery.getDriver().getId() : null;
        String status = delivery.getStatus() != null ? delivery.getStatus().toString() : null;
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(orderId)
                .driverId(driverId)
                .status(status)
                .currentLatitude(delivery.getCurrentLatitude())
                .currentLongitude(delivery.getCurrentLongitude())
                .pickupTime(delivery.getPickupTime())
                .deliveryTime(delivery.getDeliveryTime())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
