package com.example.restaurant.service;

import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.Preparation;
import com.example.restaurant.event.PreparationCompletedEvent;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PreparationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PreparationService {
    private final PreparationRepository preparationRepository;
    private final OrderRepository orderRepository;
    private final RestaurantEventProducer eventProducer;

    public void initiatePreperation(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Preparation preparation = Preparation.builder()
                    .order(order)
                    .status(Preparation.PreparationStatus.PENDING)
                    .build();

            preparationRepository.save(preparation);
            log.info("Preparation initiated for order {}", orderId);
        } catch (Exception e) {
            log.error("Error initiating preparation for order {}", orderId, e);
            throw new RuntimeException("Failed to initiate preparation", e);
        }
    }

    public void startPreparation(Long orderId) {
        try {
            Preparation preparation = preparationRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Preparation not found for order"));

            preparation.setStatus(Preparation.PreparationStatus.IN_PROGRESS);
            preparation.setStartedAt(LocalDateTime.now());
            preparationRepository.save(preparation);
            log.info("Preparation started for order {}", orderId);
        } catch (Exception e) {
            log.error("Error starting preparation for order {}", orderId, e);
            throw new RuntimeException("Failed to start preparation", e);
        }
    }

    public void completePreparation(Long orderId) {
        try {
            Preparation preparation = preparationRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Preparation not found for order"));

            preparation.setStatus(Preparation.PreparationStatus.COMPLETED);
            preparation.setCompletedAt(LocalDateTime.now());
            preparation = preparationRepository.save(preparation);

            // Update order status
            Order order = preparation.getOrder();
            order.setStatus(Order.OrderStatus.READY);
            orderRepository.save(order);

            // Publish event
            eventProducer.publishPreparationCompletedEvent(PreparationCompletedEvent.builder()
                    .preparationId(preparation.getId())
                    .orderId(orderId)
                    .status("COMPLETED")
                    .completedAt(preparation.getCompletedAt().toString())
                    .build());

            log.info("Preparation completed for order {}", orderId);
        } catch (Exception e) {
            log.error("Error completing preparation for order {}", orderId, e);
            throw new RuntimeException("Failed to complete preparation", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Preparation> getPendingPreparations() {
        return preparationRepository.findByStatus(Preparation.PreparationStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Preparation> getInProgressPreparations() {
        return preparationRepository.findByStatus(Preparation.PreparationStatus.IN_PROGRESS);
    }
}

