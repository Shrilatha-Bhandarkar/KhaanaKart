package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.MenuItemDto;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.service.MenuItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemControllerTest {

    @Mock
    private MenuItemService menuItemService;

    @Mock
    private Principal principal;

    @InjectMocks
    private MenuItemController menuItemController;

    private MenuItemDto menuItemDto;
    private final String userEmail = "owner@restaurant.com";

    @BeforeEach
    void setUp() {
        menuItemDto = new MenuItemDto();
        menuItemDto.setItemId(1L);
        menuItemDto.setRestaurantId(1L);
        menuItemDto.setName("Test Item");
        menuItemDto.setDescription("Test Description");
        menuItemDto.setPrice(new BigDecimal("10.0"));
    }

    private void setupPrincipal() {
        when(principal.getName()).thenReturn(userEmail);
    }

    @Test
    void addMenuItem_Success() {
        // Arrange
        setupPrincipal();
        when(menuItemService.addMenuItem(any(MenuItemDto.class), anyString()))
                .thenReturn(menuItemDto);

        // Act
        ResponseEntity<MenuItemDto> response = menuItemController.addMenuItem(menuItemDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Test Item", response.getBody().getName());
        verify(menuItemService).addMenuItem(any(MenuItemDto.class), eq(userEmail));
    }

    @Test
    void getMenuItemById_Success() {
        // Arrange
        when(menuItemService.getMenuItemById(anyLong())).thenReturn(menuItemDto);

        // Act
        ResponseEntity<MenuItemDto> response = menuItemController.getMenuItemById(1L);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getItemId());
        verify(menuItemService).getMenuItemById(1L);
    }

    @Test
    void getMenuItemById_NotFound() {
        // Arrange
        when(menuItemService.getMenuItemById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Menu item not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            menuItemController.getMenuItemById(1L);
        });
    }

    @Test
    void getAllMenuItemsForCategory_Success() {
        // Arrange
        MenuItemDto item1 = new MenuItemDto();
        item1.setItemId(1L);
        item1.setRestaurantId(2L);
        item1.setName("Item 1");
        item1.setDescription("Desc 1");
        item1.setPrice(new BigDecimal("10.0"));

        MenuItemDto item2 = new MenuItemDto();
        item2.setItemId(2L);
        item2.setRestaurantId(1L);
        item2.setName("Item 2");
        item2.setDescription("Desc 2");
        item2.setPrice(new BigDecimal("15.0"));

        List<MenuItemDto> items = Arrays.asList(item1, item2);
        when(menuItemService.getAllMenuItemsForCategory(anyLong(), anyLong()))
                .thenReturn(items);

        // Act
        List<MenuItemDto> result = menuItemController.getAllMenuItemsForCategory(1L, 1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Item 1", result.get(0).getName());
        verify(menuItemService).getAllMenuItemsForCategory(1L, 1L);
    }

    @Test
    void updateMenuItem_Success() {
        // Arrange
        setupPrincipal();
        when(menuItemService.updateMenuItem(anyLong(), any(MenuItemDto.class), anyString()))
                .thenReturn(menuItemDto);

        // Act
        ResponseEntity<MenuItemDto> response = menuItemController.updateMenuItem(1L, menuItemDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Test Item", response.getBody().getName());
        verify(menuItemService).updateMenuItem(eq(1L), any(MenuItemDto.class), eq(userEmail));
    }

    @Test
    void deleteMenuItem_Success() {
        // Arrange
        setupPrincipal();
        when(menuItemService.deleteMenuItem(anyLong(), anyString()))
                .thenReturn("Menu item deleted successfully");

        // Act
        ResponseEntity<String> response = menuItemController.deleteMenuItem(1L, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Menu item deleted successfully", response.getBody());
        verify(menuItemService).deleteMenuItem(1L, userEmail);
    }

    @Test
    void deleteMenuItem_Unauthorized() {
        // Arrange
        setupPrincipal();
        when(menuItemService.deleteMenuItem(anyLong(), anyString()))
                .thenThrow(new SecurityException("User not authorized to delete this menu item"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            menuItemController.deleteMenuItem(1L, principal);
        });
    }
}