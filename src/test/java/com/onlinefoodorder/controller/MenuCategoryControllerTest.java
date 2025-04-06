package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.MenuCategoryDto;
import com.onlinefoodorder.service.MenuCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCategoryControllerTest {

    @Mock
    private MenuCategoryService categoryService;

    @Mock
    private Principal principal;

    @InjectMocks
    private MenuCategoryController menuCategoryController;

    private MenuCategoryDto testCategoryDto;
    private final String testUserEmail = "owner@restaurant.com";

    @BeforeEach
    void setUp() {
        testCategoryDto = new MenuCategoryDto();
        testCategoryDto.setCategoryId(1L);
        testCategoryDto.setName("Appetizers");
        testCategoryDto.setRestaurantId(1L);
    }

    @Test
    void addCategory_ShouldReturnSuccessMessage() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        String successMessage = "Category added successfully";
        when(categoryService.createMenuCategory(any(MenuCategoryDto.class), eq(testUserEmail)))
                .thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = menuCategoryController.addCategory(testCategoryDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(successMessage, response.getBody());
        verify(categoryService).createMenuCategory(testCategoryDto, testUserEmail);
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        // Arrange
        Long restaurantId = 1L;
        Long categoryId = 1L;
        when(categoryService.getCategoryById(restaurantId, categoryId)).thenReturn(testCategoryDto);

        // Act
        ResponseEntity<MenuCategoryDto> response = 
            menuCategoryController.getCategoryById(restaurantId, categoryId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testCategoryDto, response.getBody());
        verify(categoryService).getCategoryById(restaurantId, categoryId);
    }

    @Test
    void getAllCategories_ShouldReturnCategoryList() {
        // Arrange
        Long restaurantId = 1L;
        MenuCategoryDto secondCategory = new MenuCategoryDto();
        secondCategory.setCategoryId(2L);
        secondCategory.setName("Main Course");
        secondCategory.setRestaurantId(1L);
        
        List<MenuCategoryDto> categories = Arrays.asList(testCategoryDto, secondCategory);
        when(categoryService.getAllCategoriesForRestaurant(restaurantId)).thenReturn(categories);

        // Act
        ResponseEntity<List<MenuCategoryDto>> response = 
            menuCategoryController.getAllCategories(restaurantId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Appetizers", response.getBody().get(0).getName());
        assertEquals("Main Course", response.getBody().get(1).getName());
        verify(categoryService).getAllCategoriesForRestaurant(restaurantId);
    }

    @Test
    void updateMenuCategory_ShouldReturnSuccessMessage() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        Long categoryId = 1L;
        String successMessage = "Category updated successfully";
        when(categoryService.updateCategory(eq(categoryId), any(MenuCategoryDto.class), eq(testUserEmail)))
                .thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = 
            menuCategoryController.updateMenuCategory(categoryId, testCategoryDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(successMessage, response.getBody());
        verify(categoryService).updateCategory(categoryId, testCategoryDto, testUserEmail);
    }

    @Test
    void deleteCategory_ShouldReturnSuccessMessage() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        Long categoryId = 1L;
        String successMessage = "Category deleted successfully";
        when(categoryService.deleteCategory(categoryId, testUserEmail)).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = 
            menuCategoryController.deleteCategory(categoryId, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(successMessage, response.getBody());
        verify(categoryService).deleteCategory(categoryId, testUserEmail);
    }

    @Test
    void addCategory_ShouldPassCorrectParametersToService() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        String successMessage = "Category added successfully";
        when(categoryService.createMenuCategory(eq(testCategoryDto), eq(testUserEmail)))
                .thenReturn(successMessage);

        // Act
        menuCategoryController.addCategory(testCategoryDto, principal);

        // Assert
        verify(categoryService).createMenuCategory(testCategoryDto, testUserEmail);
    }
}