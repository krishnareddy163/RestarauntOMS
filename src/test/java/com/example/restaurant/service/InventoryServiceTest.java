package com.example.restaurant.service;

import com.example.restaurant.entity.Inventory;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.User;
import com.example.restaurant.repository.InventoryRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private MenuItem menuItem;
    private Inventory inventory;

    @BeforeEach
    public void setUp() {
        menuItem = MenuItem.builder()
                .id(1L)
                .name("Burger")
                .price(new BigDecimal("5.99"))
                .available(true)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .menuItem(menuItem)
                .quantityAvailable(100)
                .quantityReserved(0)
                .lowStockThreshold(10)
                .build();
    }

    @Test
    public void testInitializeInventory() {
        // Arrange
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        inventoryService.initializeInventory(1L, 100, 10);

        // Assert
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    public void testReserveInventorySuccess() {
        // Arrange
        User customer = User.builder().id(1L).build();
        Order order = Order.builder().id(1L).customer(customer).build();
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .menuItem(menuItem)
                .quantity(5)
                .unitPrice(menuItem.getPrice())
                .build();

        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        inventoryService.reserveInventory(1L);

        // Assert
        assertEquals(5, inventory.getQuantityReserved());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    public void testReserveInventoryInsufficientStock() {
        // Arrange
        inventory.setQuantityAvailable(2);
        User customer = User.builder().id(1L).build();
        Order order = Order.builder().id(1L).customer(customer).build();
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .menuItem(menuItem)
                .quantity(5)
                .build();

        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryService.reserveInventory(1L));
    }

    @Test
    public void testGetLowStockItems() {
        // Arrange
        when(inventoryRepository.findLowStockItems()).thenReturn(List.of(inventory));

        // Act
        List<Inventory> lowStockItems = inventoryService.getLowStockItems();

        // Assert
        assertFalse(lowStockItems.isEmpty());
        assertEquals(1, lowStockItems.size());
    }
}
