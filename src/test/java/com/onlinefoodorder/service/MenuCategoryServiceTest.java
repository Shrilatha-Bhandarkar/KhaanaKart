package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.MenuCategoryDto;
import com.onlinefoodorder.entity.MenuCategory;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.MenuCategoryRepository;
import com.onlinefoodorder.repository.MenuItemRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCategoryServiceTest {

    @Mock
    private MenuCategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuCategoryService menuCategoryService;

    private User owner;
    private User customer;
    private Restaurant restaurant;
    private MenuCategory category;
    private MenuCategoryDto categoryDto;

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
        category.setDescription("Test Description");
        category.setRestaurant(restaurant);

        categoryDto = new MenuCategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Test Description");
        categoryDto.setRestaurantId(1L);
    }

    @Test
    void createMenuCategory_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(categoryRepository.save(any(MenuCategory.class))).thenReturn(category);

        String result = menuCategoryService.createMenuCategory(categoryDto, "owner@example.com");

        assertEquals("Menu category created successfully!", result);
        verify(categoryRepository).save(any(MenuCategory.class));
    }

    @Test
    void createMenuCategory_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuCategoryService.createMenuCategory(categoryDto, "owner@example.com"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void createMenuCategory_RestaurantNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuCategoryService.createMenuCategory(categoryDto, "owner@example.com"));

        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void createMenuCategory_UnauthorizedUser_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuCategoryService.createMenuCategory(categoryDto, "customer@example.com"));

        assertEquals("You are not the owner of this restaurant.", exception.getMessage());
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(menuItemRepository.countByCategoryCategoryId(anyLong())).thenReturn(5L);

        MenuCategoryDto result = menuCategoryService.getCategoryById(1L, 1L);

        assertEquals("Test Category", result.getName());
        assertEquals(5L, result.getItemCount());
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuCategoryService.getCategoryById(1L, 1L));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void getCategoryById_WrongRestaurant_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> menuCategoryService.getCategoryById(2L, 1L));

        assertEquals("Category not found for this restaurant.", exception.getMessage());
    }

    @Test
    void getAllCategoriesForRestaurant_Success() {
        when(categoryRepository.findByRestaurantRestaurantId(anyLong())).thenReturn(List.of(category));
        when(menuItemRepository.countByCategoryCategoryId(anyLong())).thenReturn(5L);

        List<MenuCategoryDto> result = menuCategoryService.getAllCategoriesForRestaurant(1L);

        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals(5L, result.get(0).getItemCount());
    }

    @Test
    void updateCategory_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(categoryRepository.save(any(MenuCategory.class))).thenReturn(category);

        String result = menuCategoryService.updateCategory(1L, categoryDto, "owner@example.com");

        assertEquals("Category updated successfully!", result);
        verify(categoryRepository).save(any(MenuCategory.class));
    }

    @Test
    void updateCategory_UnauthorizedUser_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuCategoryService.updateCategory(1L, categoryDto, "customer@example.com"));

        assertEquals("You are not the owner of this restaurant.", exception.getMessage());
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));

        String result = menuCategoryService.deleteCategory(1L, "owner@example.com");

        assertEquals("Category deleted successfully!", result);
        verify(categoryRepository).delete(any(MenuCategory.class));
    }

    @Test
    void deleteCategory_UnauthorizedUser_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> menuCategoryService.deleteCategory(1L, "customer@example.com"));

        assertEquals("You are not the owner of this restaurant.", exception.getMessage());
    }
}