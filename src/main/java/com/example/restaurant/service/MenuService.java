package com.example.restaurant.service;

import com.example.restaurant.dto.MenuItemResponse;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {
    private final MenuItemRepository menuItemRepository;

    public MenuItemResponse createMenuItem(MenuItemResponse request) {
        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .available(true)
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .build();
        
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item created: {}", saved.getId());
        return convertToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAllAvailableItems() {
        return menuItemRepository.findAllAvailable()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getItemsByCategory(String category) {
        return menuItemRepository.findByCategory(category)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void updateMenuItemAvailability(Long itemId, Boolean available) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        item.setAvailable(available);
        menuItemRepository.save(item);
        log.info("Menu item {} availability updated to {}", itemId, available);
    }

    private MenuItemResponse convertToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .available(menuItem.getAvailable())
                .preparationTimeMinutes(menuItem.getPreparationTimeMinutes())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }
}

