package com.example.restaurant.service;

import com.example.restaurant.entity.Inventory;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.repository.InventoryRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    public void initializeInventory(Long menuItemId, Integer quantity, Integer lowStockThreshold) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        
        Inventory inventory = Inventory.builder()
                .menuItem(menuItem)
                .quantityAvailable(quantity)
                .quantityReserved(0)
                .lowStockThreshold(lowStockThreshold)
                .build();
        
        inventoryRepository.save(inventory);
        log.info("Inventory initialized for menu item {}: quantity={}, lowStockThreshold={}", menuItemId, quantity, lowStockThreshold);
    }

    public void reserveInventory(Long orderId) {
        try {
            var orderItems = orderItemRepository.findByOrderId(orderId);
            for (var orderItem : orderItems) {
                Inventory inventory = inventoryRepository.findByMenuItemId(orderItem.getMenuItem().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found for item: " + orderItem.getMenuItem().getId()));
                
                if (inventory.getAvailableQuantity() < orderItem.getQuantity()) {
                    throw new RuntimeException("Insufficient inventory for item: " + orderItem.getMenuItem().getName());
                }
                
                inventory.setQuantityReserved(inventory.getQuantityReserved() + orderItem.getQuantity());
                inventoryRepository.save(inventory);
                log.info("Inventory reserved for item {}: quantity={}", orderItem.getMenuItem().getId(), orderItem.getQuantity());
            }
        } catch (Exception e) {
            log.error("Error reserving inventory for order {}", orderId, e);
            throw new RuntimeException("Failed to reserve inventory", e);
        }
    }

    public void releaseReservedInventory(Long orderId) {
        try {
            var orderItems = orderItemRepository.findByOrderId(orderId);
            for (var orderItem : orderItems) {
                Inventory inventory = inventoryRepository.findByMenuItemId(orderItem.getMenuItem().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found"));
                
                inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - orderItem.getQuantity()));
                inventory.setQuantityAvailable(Math.max(0, inventory.getQuantityAvailable() - orderItem.getQuantity()));
                inventoryRepository.save(inventory);
                log.info("Inventory released for item {}: quantity={}", orderItem.getMenuItem().getId(), orderItem.getQuantity());
            }
        } catch (Exception e) {
            log.error("Error releasing inventory for order {}", orderId, e);
            throw new RuntimeException("Failed to release inventory", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public void updateStock(Long menuItemId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByMenuItemId(menuItemId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
        inventoryRepository.save(inventory);
        log.info("Stock updated for item {}: new quantity={}", menuItemId, inventory.getQuantityAvailable());
    }
}
