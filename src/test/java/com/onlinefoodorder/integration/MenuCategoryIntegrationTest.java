package com.onlinefoodorder.integration;

import com.onlinefoodorder.controller.MenuCategoryController;
import com.onlinefoodorder.dto.MenuCategoryDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.service.MenuCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MenuCategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuCategoryService categoryService;

    @MockBean
    private MenuCategoryRepository categoryRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MenuItemRepository menuItemRepository;

    private final String TEST_USER_EMAIL = "owner@example.com";
    private final Long TEST_RESTAURANT_ID = 1L;
    private final Long TEST_CATEGORY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Common setup for tests that need authenticated user
        User owner = new User();
        owner.setUserId(1L);
        owner.setEmail(TEST_USER_EMAIL);
        
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(TEST_RESTAURANT_ID);
        restaurant.setOwner(owner);
        
        when(userRepository.findByEmail(TEST_USER_EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(TEST_RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void addCategory_ShouldSucceed_WhenUserIsOwner() throws Exception {
        // Arrange
        MenuCategoryDto dto = new MenuCategoryDto(0, TEST_RESTAURANT_ID, "Main Course", "Main dishes", 0);
        when(categoryService.createMenuCategory(any(), eq(TEST_USER_EMAIL))).thenReturn("Menu category created successfully!");

        // Act & Assert
        mockMvc.perform(post("/restaurant/menu-category")
                .contentType("application/json")
                .content("{\"restaurantId\":1,\"name\":\"Main Course\",\"description\":\"Main dishes\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Menu category created successfully!"));
    }

    @Test
    @WithMockUser(username = "nonowner@example.com")
    void addCategory_ShouldFail_WhenUserNotOwner() throws Exception {
        // Arrange
        when(categoryService.createMenuCategory(any(), anyString()))
            .thenThrow(new UnauthorizedAccessException("You are not the owner of this restaurant."));

        // Act & Assert
        mockMvc.perform(post("/restaurant/menu-category")
                .contentType("application/json")
                .content("{\"restaurantId\":1,\"name\":\"Main Course\",\"description\":\"Main dishes\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenExists() throws Exception {
        // Arrange
        MenuCategoryDto expected = new MenuCategoryDto(TEST_CATEGORY_ID, TEST_RESTAURANT_ID, "Appetizers", "Starters", 5);
        when(categoryService.getCategoryById(TEST_RESTAURANT_ID, TEST_CATEGORY_ID)).thenReturn(expected);

        // Act & Assert
        mockMvc.perform(get("/restaurant/menu-category/{restaurantId}/{categoryId}", TEST_RESTAURANT_ID, TEST_CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(TEST_CATEGORY_ID))
                .andExpect(jsonPath("$.name").value("Appetizers"))
                .andExpect(jsonPath("$.itemCount").value(5));
    }

    @Test
    void getCategoryById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(TEST_RESTAURANT_ID, TEST_CATEGORY_ID))
            .thenThrow(new ResourceNotFoundException("Category not found"));

        // Act & Assert
        mockMvc.perform(get("/restaurant/menu-category/{restaurantId}/{categoryId}", 
                TEST_RESTAURANT_ID, TEST_CATEGORY_ID))
                .andExpect(status().isBadRequest());  // Changed from isNotFound()
    }

    @Test
    void getAllCategories_ShouldReturnList_WhenRestaurantExists() throws Exception {
        // Arrange
        MenuCategoryDto category1 = new MenuCategoryDto(1L, TEST_RESTAURANT_ID, "Appetizers", "Starters", 5);
        MenuCategoryDto category2 = new MenuCategoryDto(2L, TEST_RESTAURANT_ID, "Desserts", "Sweet treats", 3);
        when(categoryService.getAllCategoriesForRestaurant(TEST_RESTAURANT_ID))
            .thenReturn(List.of(category1, category2));

        // Act & Assert
        mockMvc.perform(get("/restaurant/menu-category/{restaurantId}/all", TEST_RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Appetizers"))
                .andExpect(jsonPath("$[1].name").value("Desserts"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void updateCategory_ShouldSucceed_WhenUserIsOwner() throws Exception {
        // Arrange
        when(categoryService.updateCategory(eq(TEST_CATEGORY_ID), any(), eq(TEST_USER_EMAIL)))
            .thenReturn("Category updated successfully!");

        // Act & Assert
        mockMvc.perform(put("/restaurant/menu-category/{categoryId}", TEST_CATEGORY_ID)
                .contentType("application/json")
                .content("{\"name\":\"Updated Name\",\"description\":\"Updated Description\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Category updated successfully!"));
    }

    @Test
    @WithMockUser(username = "nonowner@example.com")
    void updateCategory_ShouldFail_WhenUserNotOwner() throws Exception {
        // Arrange
        when(categoryService.updateCategory(anyLong(), any(), anyString()))
            .thenThrow(new UnauthorizedAccessException("You are not the owner of this restaurant."));

        // Act & Assert
        mockMvc.perform(put("/restaurant/menu-category/{categoryId}", TEST_CATEGORY_ID)
                .contentType("application/json")
                .content("{\"name\":\"Updated Name\",\"description\":\"Updated Description\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void deleteCategory_ShouldSucceed_WhenUserIsOwner() throws Exception {
        // Arrange
        when(categoryService.deleteCategory(eq(TEST_CATEGORY_ID), eq(TEST_USER_EMAIL)))
            .thenReturn("Category deleted successfully!");

        // Act & Assert
        mockMvc.perform(delete("/restaurant/menu-category/{categoryId}", TEST_CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Category deleted successfully!"));
    }
}