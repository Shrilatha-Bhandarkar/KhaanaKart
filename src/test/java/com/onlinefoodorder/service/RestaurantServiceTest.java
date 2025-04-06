package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.RestaurantDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.OrderItemRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @InjectMocks
    private RestaurantService restaurantService;

    private User owner;
    private User customer;
    private RestaurantDto restaurantDto;
    private Restaurant restaurant;

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

        restaurantDto = new RestaurantDto();
        restaurantDto.setName("Test Restaurant");
        restaurantDto.setAddress("123 Test St");
        restaurantDto.setPhone("1234567890");
        restaurantDto.setRating(4.5);
        restaurantDto.setLogoUrl("http://test.com/logo.png");
        restaurantDto.setOpeningTime("09:00");
        restaurantDto.setClosingTime("21:00");

        restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setOwner(owner);
    }

    @Test
    void createRestaurant_Success() {
        restaurantService.createRestaurant(owner, restaurantDto);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void getRestaurantById_OwnerAccess_ReturnsFullDetails() {
        // Arrange
        restaurant.setAddress("123 Test St");
        restaurant.setPhone("1234567890");
        restaurant.setRating(4.5);
        restaurant.setLogoUrl("http://test.com/logo.png");
        restaurant.setOpeningTime("09:00");
        restaurant.setClosingTime("21:00");
        
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        // Act
        Map<String, Object> result = restaurantService.getRestaurantById(1L, owner);

        // Assert
        assertEquals(1L, result.get("id"));
        assertEquals("Test Restaurant", result.get("name"));
        assertEquals("123 Test St", result.get("address"));
        assertEquals("1234567890", result.get("phone"));
        assertEquals(4.5, result.get("rating"));
        assertEquals("http://test.com/logo.png", result.get("logoUrl"));
        assertEquals("09:00", result.get("openingTime"));
        assertEquals("21:00", result.get("closingTime"));
        assertEquals(1L, result.get("ownerId"));
    }

    @Test
    void getRestaurantById_CustomerAccess_ReturnsLimitedDetails() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        Map<String, Object> result = restaurantService.getRestaurantById(1L, customer);

        assertEquals(5, result.size());
        assertEquals("Test Restaurant", result.get("name"));
        assertNull(result.get("ownerId"));
    }

    @Test
    void getRestaurantById_NotFound_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> restaurantService.getRestaurantById(1L, owner));
        
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void updateRestaurant_Owner_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        String result = restaurantService.updateRestaurant(1L, restaurantDto, owner);

        assertEquals("Restaurant updated successfully!", result);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_NonOwner_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> restaurantService.updateRestaurant(1L, restaurantDto, customer));
        
        assertEquals("Unauthorized access to update restaurant.", exception.getMessage());
    }

    @Test
    void updateRestaurant_NotFound_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> restaurantService.updateRestaurant(1L, restaurantDto, owner));
        
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void deleteRestaurant_Owner_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        String result = restaurantService.deleteRestaurant(1L, owner);

        assertEquals("Restaurant deleted successfully!", result);
        verify(restaurantRepository).delete(any(Restaurant.class));
    }

    @Test
    void deleteRestaurant_NonOwner_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> restaurantService.deleteRestaurant(1L, customer));
        
        assertEquals("Unauthorized access to delete restaurant.", exception.getMessage());
    }

    @Test
    void getAllRestaurants_Success() {
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<Map<String, Object>> result = restaurantService.getAllRestaurants();

        assertEquals(1, result.size());
        assertEquals("Test Restaurant", result.get(0).get("name"));
    }

    @Test
    void getAllRestaurants_EmptyList() {
        when(restaurantRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = restaurantService.getAllRestaurants();

        assertTrue(result.isEmpty());
    }

    @Test
    void getRestaurantEntityById_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.getRestaurantEntityById(1L);

        assertEquals("Test Restaurant", result.getName());
    }

    @Test
    void getRestaurantEntityById_NotFound_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> restaurantService.getRestaurantEntityById(1L));
        
        assertEquals("Restaurant not found", exception.getMessage());
    }
    
    @Test
    void getMenuItemSalesChart_WithData_ReturnsChartBytes() {
        // Arrange
        String email = "owner@example.com";
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{"Burger", 50L});
        mockData.add(new Object[]{"Pizza", 30L});
        when(orderItemRepository.findSalesStatsByOwner(email)).thenReturn(mockData);

        // Act
        byte[] result = restaurantService.getMenuItemSalesChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderItemRepository, times(1)).findSalesStatsByOwner(email);
    }

    @Test
    void getMenuItemSalesChart_EmptyData_ReturnsEmptyChart() {
        // Arrange
        String email = "owner@example.com";
        when(orderItemRepository.findSalesStatsByOwner(email)).thenReturn(new ArrayList<>());

        // Act
        byte[] result = restaurantService.getMenuItemSalesChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderItemRepository, times(1)).findSalesStatsByOwner(email);
    }

    @Test
    void getMenuItemSalesChart_NullEmail_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> restaurantService.getMenuItemSalesChart(null));
        
        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    void getMenuItemSalesChart_EmptyEmail_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> restaurantService.getMenuItemSalesChart(""));
        
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void getMenuItemSalesChart_BlankEmail_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> restaurantService.getMenuItemSalesChart("   "));
        
        assertEquals("Email cannot be empty", exception.getMessage());
    }
}