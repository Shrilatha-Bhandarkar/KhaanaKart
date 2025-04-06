package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.MenuItemDto;
import com.onlinefoodorder.entity.MenuCategory;
import com.onlinefoodorder.entity.MenuItem;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.MenuCategoryRepository;
import com.onlinefoodorder.repository.MenuItemRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.UserRole;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuCategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    private User owner;
    private User customer;
    private Restaurant restaurant;
    private MenuCategory category;
    private MenuItem menuItem;
    private MenuItemDto menuItemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUserId(1L);
        owner.setEmail("owner@example.com");
        owner.setRole(UserRole.RESTAURANT_OWNER);

        customer = new User();
        customer.setUserId(2L);
        customer.setEmail("customer@example.com");
        customer.setRole(UserRole.CUSTOMER);

        restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setOwner(owner);

        category = new MenuCategory();
        category.setCategoryId(1L);
        category.setName("Test Category");
        category.setRestaurant(restaurant);

        menuItem = new MenuItem();
        menuItem.setItemId(1L);
        menuItem.setName("Test Item");
        menuItem.setDescription("Test Description");
        menuItem.setPrice(new BigDecimal("9.99"));
        menuItem.setVegetarian(true);
        menuItem.setAvailable(true);
        menuItem.setPreparationTimeMin(15);
        menuItem.setCategory(category);
        menuItem.setRestaurant(restaurant);

        menuItemDto = new MenuItemDto();
        menuItemDto.setName("Test Item");
        menuItemDto.setDescription("Test Description");
        menuItem.setPrice(new BigDecimal("9.99"));
        menuItemDto.setVegetarian(true);
        menuItemDto.setAvailable(true);
        menuItemDto.setPreparationTimeMin(15);
        menuItemDto.setCategoryId(1L);
        menuItemDto.setRestaurantId(1L);
    }

    @Test
    void addMenuItem_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemDto result = menuItemService.addMenuItem(menuItemDto, "owner@example.com");

        assertEquals("Test Item", result.getName());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void addMenuItem_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(menuItemDto, "owner@example.com"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addMenuItem_CategoryNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(menuItemDto, "owner@example.com"));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void addMenuItem_UnauthorizedUser_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuItemService.addMenuItem(menuItemDto, "customer@example.com"));

        assertEquals("You are not authorized to add menu items to this restaurant", exception.getMessage());
    }

    @Test
    void getMenuItemById_Success() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));

        MenuItemDto result = menuItemService.getMenuItemById(1L);

        assertEquals("Test Item", result.getName());
        assertEquals(new BigDecimal("9.99"), result.getPrice());
    }

    @Test
    void getMenuItemById_NotFound_ThrowsException() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.getMenuItemById(1L));

        assertEquals("Menu Item not found", exception.getMessage());
    }

    @Test
    void getAllMenuItemsForCategory_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(menuItemRepository.findByCategoryCategoryId(anyLong())).thenReturn(List.of(menuItem));

        List<MenuItemDto> result = menuItemService.getAllMenuItemsForCategory(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
    }

    @Test
    void getAllMenuItemsForCategory_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.getAllMenuItemsForCategory(1L, 1L));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void getAllMenuItemsForCategory_RestaurantMismatch_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuItemService.getAllMenuItemsForCategory(2L, 1L));

        assertEquals("Category does not belong to the specified restaurant", exception.getMessage());
    }

    @Test
    void updateMenuItem_Success() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemDto result = menuItemService.updateMenuItem(1L, menuItemDto, "owner@example.com");

        assertEquals("Test Item", result.getName());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void updateMenuItem_NotFound_ThrowsException() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.updateMenuItem(1L, menuItemDto, "owner@example.com"));

        assertEquals("Menu item not found", exception.getMessage());
    }

    @Test
    void updateMenuItem_UnauthorizedUser_ThrowsException() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuItemService.updateMenuItem(1L, menuItemDto, "customer@example.com"));

        assertEquals("You are not the owner of this restaurant.", exception.getMessage());
    }

    @Test
    void updateMenuItem_RestaurantMismatch_ThrowsException() {
        menuItemDto.setRestaurantId(2L);
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.updateMenuItem(1L, menuItemDto, "owner@example.com"));

        assertEquals("Restaurant mismatch", exception.getMessage());
    }

    @Test
    void deleteMenuItem_Success() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));

        String result = menuItemService.deleteMenuItem(1L, "owner@example.com");

        assertEquals("Menu item deleted successfully!", result);
        verify(menuItemRepository).delete(any(MenuItem.class));
    }

    @Test
    void deleteMenuItem_NotFound_ThrowsException() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.deleteMenuItem(1L, "owner@example.com"));

        assertEquals("Menu item not found", exception.getMessage());
    }

    @Test
    void deleteMenuItem_UnauthorizedUser_ThrowsException() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuItemService.deleteMenuItem(1L, "customer@example.com"));

        assertEquals("You are not the owner of this restaurant.", exception.getMessage());
    }
}