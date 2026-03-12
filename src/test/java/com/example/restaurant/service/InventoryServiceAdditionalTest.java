package com.example.restaurant.service;

import com.example.restaurant.entity.Inventory;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Order;
import com.example.restaurant.entity.OrderItem;
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
class InventoryServiceAdditionalTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        menuItem = MenuItem.builder().id(1L).name("Burger").price(new BigDecimal("9.99")).available(true).build();
    }

    @Test
    void initializeInventory_success() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.initializeInventory(1L, 10, 2);

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void initializeInventory_missingMenuItem_throws() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inventoryService.initializeInventory(1L, 10, 2));
    }

    @Test
    void reserveInventory_success() {
        Order order = Order.builder().id(1L).build();
        OrderItem item = OrderItem.builder().order(order).menuItem(menuItem).quantity(2).unitPrice(menuItem.getPrice()).build();
        Inventory inventory = Inventory.builder().menuItem(menuItem).quantityAvailable(10).quantityReserved(0).build();
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);

        inventoryService.reserveInventory(1L);

        assertEquals(2, inventory.getQuantityReserved());
    }

    @Test
    void reserveInventory_insufficient_throws() {
        Order order = Order.builder().id(1L).build();
        OrderItem item = OrderItem.builder().order(order).menuItem(menuItem).quantity(5).unitPrice(menuItem.getPrice()).build();
        Inventory inventory = Inventory.builder().menuItem(menuItem).quantityAvailable(3).quantityReserved(0).build();
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));

        assertThrows(RuntimeException.class, () -> inventoryService.reserveInventory(1L));
    }

    @Test
    void reserveInventory_repositoryError_throws() {
        when(orderItemRepository.findByOrderId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> inventoryService.reserveInventory(1L));
    }

    @Test
    void releaseReservedInventory_success() {
        Order order = Order.builder().id(1L).build();
        OrderItem item = OrderItem.builder().order(order).menuItem(menuItem).quantity(2).unitPrice(menuItem.getPrice()).build();
        Inventory inventory = Inventory.builder().menuItem(menuItem).quantityAvailable(10).quantityReserved(3).build();
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);

        inventoryService.releaseReservedInventory(1L);

        assertEquals(1, inventory.getQuantityReserved());
        assertEquals(8, inventory.getQuantityAvailable());
    }

    @Test
    void releaseReservedInventory_repositoryError_throws() {
        when(orderItemRepository.findByOrderId(1L)).thenThrow(new RuntimeException("db"));
        assertThrows(RuntimeException.class, () -> inventoryService.releaseReservedInventory(1L));
    }

    @Test
    void updateStock_success() {
        Inventory inventory = Inventory.builder().menuItem(menuItem).quantityAvailable(5).quantityReserved(0).build();
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);

        inventoryService.updateStock(1L, 3);

        assertEquals(8, inventory.getQuantityAvailable());
    }

    @Test
    void updateStock_missing_throws() {
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inventoryService.updateStock(1L, 3));
    }
}
