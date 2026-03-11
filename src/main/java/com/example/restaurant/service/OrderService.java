package com.example.restaurant.service;

import com.example.restaurant.dto.CreateOrderRequest;
import com.example.restaurant.dto.OrderResponse;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.User;
import com.example.restaurant.event.OrderCreatedEvent;
import com.example.restaurant.kafka.RestaurantEventProducer;
import com.example.restaurant.repository.OrderRepository;
import com.example.restaurant.repository.OrderItemRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final RestaurantEventProducer eventProducer;

    public OrderResponse createOrder(CreateOrderRequest request) {
        try {
            // Validate customer
            User customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Create order
            BigDecimal totalAmount = BigDecimal.ZERO;
            Order order = Order.builder()
                    .customer(customer)
                    .status(Order.OrderStatus.PENDING)
                    .deliveryAddress(request.getDeliveryAddress())
                    .specialInstructions(request.getSpecialInstructions())
                    .deliveryType(Order.DeliveryType.valueOf(request.getDeliveryType()))
                    .build();

            order = orderRepository.save(order);
            log.info("Order created with ID: {}", order.getId());

            // Add order items
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                        .orElseThrow(() -> new RuntimeException("Menu item not found"));

                if (!menuItem.getAvailable()) {
                    throw new RuntimeException("Menu item is not available: " + menuItem.getName());
                }

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .menuItem(menuItem)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(menuItem.getPrice())
                        .specialInstructions(itemRequest.getSpecialInstructions())
                        .build();

                orderItemRepository.save(orderItem);
                totalAmount = totalAmount.add(orderItem.getTotalPrice());
            }

            // Update order total
            order.setTotalAmount(totalAmount);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order = orderRepository.save(order);

            // Reserve inventory
            inventoryService.reserveInventory(order.getId());

            // Publish event
            eventProducer.publishOrderCreatedEvent(OrderCreatedEvent.builder()
                    .orderId(order.getId())
                    .customerId(customer.getId())
                    .totalAmount(totalAmount.toString())
                    .deliveryType(request.getDeliveryType())
                    .createdAt(order.getCreatedAt().toString())
                    .build());

            log.info("Order {} created and event published", order.getId());
            return convertToResponse(order);
        } catch (Exception e) {
            log.error("Error creating order", e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToResponse);
    }

    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Order.OrderStatus.valueOf(status));
        if (status.equals("DELIVERED")) {
            order.setCompletedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, status);
    }

    public Page<OrderResponse> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::convertToResponse);
    }

    private OrderResponse convertToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(oi -> OrderResponse.OrderItemResponse.builder()
                        .id(oi.getId())
                        .menuItemId(oi.getMenuItem().getId())
                        .menuItemName(oi.getMenuItem().getName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .specialInstructions(oi.getSpecialInstructions())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus().toString())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .deliveryType(order.getDeliveryType().toString())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .items(items)
                .build();
    }
}
