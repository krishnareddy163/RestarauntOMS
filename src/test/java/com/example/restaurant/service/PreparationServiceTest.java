package com.example.restaurant.service;

import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.Preparation;
import com.example.restaurant.event.PreparationCompletedEvent;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.PreparationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreparationServiceTest {

    @Mock
    private PreparationRepository preparationRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantEventProducer eventProducer;

    @InjectMocks
    private PreparationService preparationService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder().id(1L).status(Order.OrderStatus.CONFIRMED).build();
    }

    @Test
    void initiatePreparation_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(preparationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        preparationService.initiatePreperation(1L);

        verify(preparationRepository, times(1)).save(any(Preparation.class));
    }

    @Test
    void initiatePreparation_missingOrder_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> preparationService.initiatePreperation(1L));
    }

    @Test
    void startPreparation_success() {
        Preparation prep = Preparation.builder().order(order).status(Preparation.PreparationStatus.PENDING).build();
        when(preparationRepository.findByOrderId(1L)).thenReturn(Optional.of(prep));
        when(preparationRepository.save(any())).thenReturn(prep);

        preparationService.startPreparation(1L);

        assertEquals(Preparation.PreparationStatus.IN_PROGRESS, prep.getStatus());
        assertNotNull(prep.getStartedAt());
    }

    @Test
    void startPreparation_missing_throws() {
        when(preparationRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> preparationService.startPreparation(1L));
    }

    @Test
    void completePreparation_success() {
        Preparation prep = Preparation.builder().id(5L).order(order).status(Preparation.PreparationStatus.IN_PROGRESS).build();
        when(preparationRepository.findByOrderId(1L)).thenReturn(Optional.of(prep));
        when(preparationRepository.save(any())).thenReturn(prep);
        when(orderRepository.save(any())).thenReturn(order);

        preparationService.completePreparation(1L);

        assertEquals(Preparation.PreparationStatus.COMPLETED, prep.getStatus());
        assertNotNull(prep.getCompletedAt());
        assertEquals(Order.OrderStatus.READY, order.getStatus());
        verify(eventProducer, times(1)).publishPreparationCompletedEvent(any(PreparationCompletedEvent.class));
    }

    @Test
    void completePreparation_missing_throws() {
        when(preparationRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> preparationService.completePreparation(1L));
    }

    @Test
    void getPendingPreparations_returnsList() {
        when(preparationRepository.findByStatus(Preparation.PreparationStatus.PENDING))
                .thenReturn(List.of(Preparation.builder().build()));
        assertEquals(1, preparationService.getPendingPreparations().size());
    }

    @Test
    void getInProgressPreparations_returnsList() {
        when(preparationRepository.findByStatus(Preparation.PreparationStatus.IN_PROGRESS))
                .thenReturn(List.of(Preparation.builder().build()));
        assertEquals(1, preparationService.getInProgressPreparations().size());
    }
}
