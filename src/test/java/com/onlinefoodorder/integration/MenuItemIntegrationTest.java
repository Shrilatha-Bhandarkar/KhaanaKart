package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.dto.MenuItemDto;
import com.onlinefoodorder.entity.MenuCategory;
import com.onlinefoodorder.entity.MenuItem;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.MenuCategoryRepository;
import com.onlinefoodorder.repository.MenuItemRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MenuItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User owner;
    private Restaurant restaurant;
    private MenuCategory category;

    @BeforeEach
    public void setup() {
        menuItemRepository.deleteAll();
        categoryRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.flush();
        // Create and save User
        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setUsername("owner1");
        owner.setPassword("pass");
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setPhone("1234567890");
        owner.setRole(UserRole.RESTAURANT_OWNER);
        owner.setActive(true);
        owner = userRepository.save(owner); // Save and reassign for ID

        // Create and save Restaurant
        restaurant = new Restaurant();
        restaurant.setName("Testaurant");
        restaurant.setAddress("123 Test St");
        restaurant.setPhone("9876543210");
        restaurant.setRating(4.5);
        restaurant.setLogoUrl("https://example.com/logo.png");
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setOpeningTime("09:00");
        restaurant.setClosingTime("22:00");
        restaurant.setOwner(owner);
        restaurant = restaurantRepository.save(restaurant);

        // Create and save Category
        category = new MenuCategory();
        category.setName("Starters");
        category.setDescription("Appetizers");
        category.setRestaurant(restaurant);
        category.setMenuItems(Collections.emptyList());
        category = categoryRepository.save(category);
        
        restaurantRepository.save(restaurant);
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    public void testAddMenuItem() throws Exception {
        MenuItemDto dto = new MenuItemDto(
                0L,
                category.getCategoryId(),
                restaurant.getRestaurantId(),
                "Spring Rolls",
                "Crispy and delicious",
                new BigDecimal("5.99"),
                "img.jpg",
                true,
                true,
                15
        );

        mockMvc.perform(post("/restaurant/menu-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spring Rolls"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    public void testGetMenuItemById() throws Exception {
        // Create item first
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setRestaurant(restaurant);
        item.setName("Fried Rice");
        item.setDescription("Spicy");
        item.setPrice(new BigDecimal("7.50"));
        item.setImageUrl("image.jpg");
        item.setVegetarian(false);
        item.setAvailable(true);
        item.setPreparationTimeMin(20);
        menuItemRepository.save(item);

        mockMvc.perform(get("/restaurant/menu-item/" + item.getItemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fried Rice"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    public void testUpdateMenuItem() throws Exception {
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setRestaurant(restaurant);
        item.setName("Pizza");
        item.setDescription("Cheesy");
        item.setPrice(new BigDecimal("9.99"));
        item.setImageUrl("pizza.jpg");
        item.setVegetarian(false);
        item.setAvailable(true);
        item.setPreparationTimeMin(25);
        item = menuItemRepository.save(item);

        MenuItemDto dto = new MenuItemDto(
                item.getItemId(),
                category.getCategoryId(),
                restaurant.getRestaurantId(),
                "Veg Pizza",
                "Cheesy with veggies",
                new BigDecimal("10.99"),
                "veg_pizza.jpg",
                true,
                true,
                20
        );

        mockMvc.perform(put("/restaurant/menu-item/" + item.getItemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Veg Pizza"))
                .andExpect(jsonPath("$.vegetarian").value(true));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    public void testDeleteMenuItem() throws Exception {
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setRestaurant(restaurant);
        item.setName("Soup");
        item.setDescription("Hot soup");
        item.setPrice(new BigDecimal("3.99"));
        item.setImageUrl("soup.jpg");
        item.setVegetarian(true);
        item.setAvailable(true);
        item.setPreparationTimeMin(10);
        item = menuItemRepository.save(item);

        mockMvc.perform(delete("/restaurant/menu-item/" + item.getItemId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Menu item deleted successfully!"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    public void testGetAllMenuItemsForCategory() throws Exception {
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setRestaurant(restaurant);
        item.setName("Burger");
        item.setDescription("Tasty");
        item.setPrice(new BigDecimal("6.99"));
        item.setImageUrl("burger.jpg");
        item.setVegetarian(false);
        item.setAvailable(true);
        item.setPreparationTimeMin(15);
        menuItemRepository.save(item);

        mockMvc.perform(get("/restaurant/menu-item/" + restaurant.getRestaurantId() + "/" + category.getCategoryId() + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Burger"));
    }
}
