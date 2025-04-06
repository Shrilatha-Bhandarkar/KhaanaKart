package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.RestaurantDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.service.RestaurantService;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

	@Mock
	private RestaurantService restaurantService;

	@Mock
	private UserService userService;

	@Mock
	private Principal principal;

	@InjectMocks
	private RestaurantController restaurantController;

	private User approvedOwner;
	private User pendingOwner;
	private User customer;
	private RestaurantDto restaurantDto;

	@BeforeEach
	void setUp() {
		approvedOwner = new User();
		approvedOwner.setEmail("owner@example.com");
		approvedOwner.setRole(UserRole.RESTAURANT_OWNER);
		approvedOwner.setApprovalStatus(ApprovalStatus.APPROVED);

		pendingOwner = new User();
		pendingOwner.setEmail("pending@example.com");
		pendingOwner.setRole(UserRole.RESTAURANT_OWNER);
		pendingOwner.setApprovalStatus(ApprovalStatus.PENDING);

		customer = new User();
		customer.setEmail("customer@example.com");
		customer.setRole(UserRole.CUSTOMER);
		customer.setApprovalStatus(ApprovalStatus.APPROVED);

		restaurantDto = new RestaurantDto();
		restaurantDto.setName("Test Restaurant");
		restaurantDto.setAddress("Test Location");
	}

	@Test
	void addRestaurant_Success() {
		// Arrange
		when(principal.getName()).thenReturn("owner@example.com");
		when(userService.getUserByEmail("owner@example.com")).thenReturn(approvedOwner);
		doNothing().when(restaurantService).createRestaurant(approvedOwner, restaurantDto);

		// Act
		ResponseEntity<String> response = restaurantController.addRestaurant(restaurantDto, principal);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Restaurant added successfully!", response.getBody());
		verify(restaurantService, times(1)).createRestaurant(approvedOwner, restaurantDto);
	}

	@Test
	void addRestaurant_UnauthorizedRole() {
		// Arrange
		when(principal.getName()).thenReturn("customer@example.com");
		when(userService.getUserByEmail("customer@example.com")).thenReturn(customer);

		// Act
		ResponseEntity<String> response = restaurantController.addRestaurant(restaurantDto, principal);

		// Assert
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("Only restaurant owners can add restaurants.", response.getBody());
		verify(restaurantService, never()).createRestaurant(any(), any());
	}

	@Test
	void addRestaurant_PendingApproval() {
		// Arrange
		when(principal.getName()).thenReturn("pending@example.com");
		when(userService.getUserByEmail("pending@example.com")).thenReturn(pendingOwner);

		// Act
		ResponseEntity<String> response = restaurantController.addRestaurant(restaurantDto, principal);

		// Assert
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("Your account is pending admin approval.", response.getBody());
		verify(restaurantService, never()).createRestaurant(any(), any());
	}

	@Test
	void getRestaurant_AuthenticatedUser() {
		// Arrange
		Long restaurantId = 1L;
		when(principal.getName()).thenReturn("customer@example.com");
		when(userService.getUserByEmail("customer@example.com")).thenReturn(customer);
		when(restaurantService.getRestaurantById(restaurantId, customer))
				.thenReturn(Collections.singletonMap("restaurant", "Test Restaurant"));

		// Act
		ResponseEntity<Map<String, Object>> response = restaurantController.getRestaurant(restaurantId, principal);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Test Restaurant", response.getBody().get("restaurant"));
	}

	@Test
	void getRestaurant_AnonymousUser() {
		// Arrange
		Long restaurantId = 1L;
		when(restaurantService.getRestaurantById(restaurantId, null))
				.thenReturn(Collections.singletonMap("restaurant", "Test Restaurant"));

		// Act
		ResponseEntity<Map<String, Object>> response = restaurantController.getRestaurant(restaurantId, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Test Restaurant", response.getBody().get("restaurant"));
	}

	@Test
	void updateRestaurant_Success() {
		// Arrange
		Long restaurantId = 1L;
		when(principal.getName()).thenReturn("owner@example.com");
		when(userService.getUserByEmail("owner@example.com")).thenReturn(approvedOwner);
		when(restaurantService.updateRestaurant(restaurantId, restaurantDto, approvedOwner))
				.thenReturn("Restaurant updated successfully");

		// Act
		ResponseEntity<String> response = restaurantController.updateRestaurant(restaurantId, restaurantDto, principal);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Restaurant updated successfully", response.getBody());
	}

	@Test
	void deleteRestaurant_Success() {
		// Arrange
		Long restaurantId = 1L;
		when(principal.getName()).thenReturn("owner@example.com");
		when(userService.getUserByEmail("owner@example.com")).thenReturn(approvedOwner);
		when(restaurantService.deleteRestaurant(restaurantId, approvedOwner))
				.thenReturn("Restaurant deleted successfully");

		// Act
		ResponseEntity<String> response = restaurantController.deleteRestaurant(restaurantId, principal);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Restaurant deleted successfully", response.getBody());
	}

	@Test
	void deleteRestaurant_Unauthenticated() {
		// Arrange
		Long restaurantId = 1L;

		// Act & Assert
		assertThrows(UnauthorizedAccessException.class, () -> {
			restaurantController.deleteRestaurant(restaurantId, null);
		});
	}

	@Test
	void getAllRestaurants_Success() {
		// Arrange
		List<Map<String, Object>> restaurants = Collections
				.singletonList(Collections.singletonMap("name", "Test Restaurant"));
		when(restaurantService.getAllRestaurants()).thenReturn(restaurants);

		// Act
		ResponseEntity<List<Map<String, Object>>> response = restaurantController.getAllRestaurants();

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
		assertEquals("Test Restaurant", response.getBody().get(0).get("name"));
	}

	@Test
	void getMenuSalesChart_Success() throws Exception {
		// Arrange
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("owner@example.com");

		byte[] mockChart = new byte[] { 1, 2, 3 }; // dummy image data
		when(restaurantService.getMenuItemSalesChart("owner@example.com")).thenReturn(mockChart);

		// Act
		ResponseEntity<byte[]> response = restaurantController.getMenuSalesChart(authentication);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertArrayEquals(mockChart, response.getBody());
		verify(restaurantService, times(1)).getMenuItemSalesChart("owner@example.com");
	}

	@Test
	void getMenuSalesChart_Unauthenticated() {
	    // Act & Assert
	    UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, 
	        () -> restaurantController.getMenuSalesChart(null));
	    
	    assertEquals("Authentication required", exception.getMessage());
	}

	@Test
	void getMenuSalesChart_EmptyChartData() {
		// Arrange
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("owner@example.com");

		byte[] emptyChart = new byte[0];
		when(restaurantService.getMenuItemSalesChart("owner@example.com")).thenReturn(emptyChart);

		// Act
		ResponseEntity<byte[]> response = restaurantController.getMenuSalesChart(authentication);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
		assertArrayEquals(emptyChart, response.getBody());
	}

	@Test
	void getMenuSalesChart_ServiceThrowsException() {
	    // Arrange
	    Authentication authentication = mock(Authentication.class);
	    when(authentication.getName()).thenReturn("owner@example.com");
	    
	    when(restaurantService.getMenuItemSalesChart("owner@example.com"))
	        .thenThrow(new RuntimeException("Chart generation failed"));

	    // Act & Assert
	    assertThrows(RuntimeException.class, () -> {
	        restaurantController.getMenuSalesChart(authentication);
	    });
	}
}