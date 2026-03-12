package com.example.restaurant.service;

import com.example.restaurant.dto.MenuItemResponse;
import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.repository.MenuItemRepository;
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
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        menuItem = MenuItem.builder()
                .id(1L)
                .name("Burger")
                .description("Test")
                .price(new BigDecimal("9.99"))
                .category("Main")
                .available(true)
                .preparationTimeMinutes(10)
                .build();
    }

    @Test
    void createMenuItem_success() {
        MenuItemResponse request = MenuItemResponse.builder()
                .name("Burger")
                .description("Test")
                .price(new BigDecimal("9.99"))
                .category("Main")
                .preparationTimeMinutes(10)
                .build();
        when(menuItemRepository.save(any())).thenReturn(menuItem);

        MenuItemResponse response = menuService.createMenuItem(request);

        assertNotNull(response);
        assertEquals("Burger", response.getName());
        verify(menuItemRepository, times(1)).save(any());
    }

    @Test
    void getAllAvailableItems_returnsList() {
        when(menuItemRepository.findAllAvailable()).thenReturn(List.of(menuItem));

        List<MenuItemResponse> items = menuService.getAllAvailableItems();

        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId());
    }

    @Test
    void getItemsByCategory_returnsList() {
        when(menuItemRepository.findByCategory("Main")).thenReturn(List.of(menuItem));

        List<MenuItemResponse> items = menuService.getItemsByCategory("Main");

        assertEquals(1, items.size());
        assertEquals("Main", items.get(0).getCategory());
    }

    @Test
    void updateMenuItemAvailability_success() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any())).thenReturn(menuItem);

        menuService.updateMenuItemAvailability(1L, false);

        assertFalse(menuItem.getAvailable());
        verify(menuItemRepository, times(1)).save(menuItem);
    }

    @Test
    void updateMenuItemAvailability_missing_throws() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> menuService.updateMenuItemAvailability(1L, false));
    }
}
