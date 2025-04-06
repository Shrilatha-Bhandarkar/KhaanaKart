package com.onlinefoodorder.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.onlinefoodorder.dto.RestaurantDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.service.RestaurantService;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;

/**
 * Controller for managing restaurant operations such as adding, retrieving,
 * updating, and deleting restaurants.
 */
@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

	private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

	@Autowired
	private RestaurantService restaurantService;

	@Autowired
	private UserService userService;

	/**
	 * Adds a new restaurant for the user with the role "RESTAURANT_OWNER" and
	 * "APPROVED" status.
	 * 
	 * @param restaurantDto DTO containing restaurant details
	 * @param principal     Current logged-in user
	 * @return ResponseEntity with success or failure message
	 */
	@PostMapping("/add")
	public ResponseEntity<String> addRestaurant(@RequestBody RestaurantDto restaurantDto, Principal principal) {
		logger.info("Creating restaurant: {}", restaurantDto.getName());

		User user = userService.getUserByEmail(principal.getName());

		// Check if the user has the "RESTAURANT_OWNER" role
		if (!user.getRole().equals(UserRole.RESTAURANT_OWNER)) {
			logger.error("User {} is not authorized to add restaurants.", user.getEmail());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only restaurant owners can add restaurants.");
		}

		// Check if the user's approval status is "APPROVED"
		if (!user.getApprovalStatus().equals(ApprovalStatus.APPROVED)) {
			logger.error("User {} has not been approved by admin.", user.getEmail());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is pending admin approval.");
		}

		// Proceed to create the restaurant
		restaurantService.createRestaurant(user, restaurantDto);
		logger.info("Restaurant {} added successfully by user {}.", restaurantDto.getName(), user.getEmail());
		return ResponseEntity.ok("Restaurant added successfully!");
	}

	/**
	 * Retrieves a restaurant by its ID.
	 * 
	 * @param id Restaurant ID
	 * @return ResponseEntity containing the restaurant details
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> getRestaurant(@PathVariable Long id, Principal principal) {
	    User user = null;
	    
	    if (principal != null) {
	        user = userService.getUserByEmail(principal.getName());
	    }

	    Map<String, Object> response = restaurantService.getRestaurantById(id, user);
	    return ResponseEntity.ok(response);
	}


	/**
	 * Updates the details of an existing restaurant.
	 * 
	 * @param id        Restaurant ID
	 * @param dto       DTO containing updated restaurant details
	 * @param principal Current logged-in user
	 * @return ResponseEntity with success or failure message
	 */
	@PutMapping("/{id}")
	public ResponseEntity<String> updateRestaurant(@PathVariable Long id, @RequestBody RestaurantDto dto,
			Principal principal) {
		User user = userService.getUserByEmail(principal.getName());
		logger.info("User {} is updating restaurant with ID: {}", user.getEmail(), id);
		return ResponseEntity.ok(restaurantService.updateRestaurant(id, dto, user));
	}

	/**
	 * Deletes a restaurant by its ID.
	 * 
	 * @param id        Restaurant ID
	 * @param principal Current logged-in user
	 * @return ResponseEntity with success or failure message
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteRestaurant(@PathVariable Long id, Principal principal) {
		if (principal == null) {
			logger.error("Unauthorized attempt to delete restaurant with ID {}", id);
			throw new UnauthorizedAccessException("User not authenticated!");
		}

		User user = userService.getUserByEmail(principal.getName());
		return ResponseEntity.ok(restaurantService.deleteRestaurant(id, user));
	}

	/**
	 * Deletes a restaurant by its ID.
	 * 
	 * @param id        The unique identifier of the restaurant to be deleted.
	 * @param principal The currently logged-in user initiating the deletion.
	 * @return ResponseEntity containing a success or failure message.
	 */
	@GetMapping("/all")
	public ResponseEntity<List<Map<String, Object>>> getAllRestaurants() {
		logger.info("Fetching all restaurants");

		List<Map<String, Object>> restaurants = restaurantService.getAllRestaurants();

		logger.info("Successfully fetched {} restaurants", restaurants.size());
		return ResponseEntity.ok(restaurants);
	}
	
	@GetMapping("/menu/sales-chart")
	public ResponseEntity<byte[]> getMenuSalesChart(Authentication authentication) {
	    if (authentication == null) {
	        throw new UnauthorizedAccessException("Authentication required");
	    }
	    
	    String username = authentication.getName();
	    byte[] chart = restaurantService.getMenuItemSalesChart(username);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_PNG);
	    return new ResponseEntity<>(chart, headers, HttpStatus.OK);
	}

}
