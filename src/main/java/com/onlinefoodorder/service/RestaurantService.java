package com.onlinefoodorder.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.onlinefoodorder.dto.RestaurantDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.OrderItemRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.util.Charts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class that handles restaurant-related business logic such as
 * creating, updating, and deleting restaurants.
 */
@Service
public class RestaurantService {

	private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private OrderItemRepository orderitemRepository;

	/**
	 * Creates a new restaurant and saves it in the database.
	 * 
	 * @param owner User who owns the restaurant
	 * @param dto   DTO containing the restaurant details
	 */
	public void createRestaurant(User owner, RestaurantDto dto) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(dto.getName());
		restaurant.setAddress(dto.getAddress());
		restaurant.setPhone(dto.getPhone());
		restaurant.setLogoUrl(dto.getLogoUrl());
		restaurant.setOpeningTime(dto.getOpeningTime());
		restaurant.setClosingTime(dto.getClosingTime());
		restaurant.setOwner(owner);

		restaurantRepository.save(restaurant);
		logger.info("Restaurant '{}' created by owner '{}'.", restaurant.getName(), owner.getEmail());
	}

	/**
	 * Retrieves a restaurant by its ID.
	 * 
	 * @param id Restaurant ID
	 * @return Restaurant entity
	 */
	public Map<String, Object> getRestaurantById(long id, User user) {
		Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
			logger.error("Restaurant with ID {} not found.", id);
			return new RuntimeException("Restaurant not found");
		});

		Map<String, Object> response = new HashMap<>();

		// If the user is the restaurant owner, return full details
		if (user != null && restaurant.getOwner().getUserId().equals(user.getUserId())) {
			response.put("id", restaurant.getRestaurantId());
			response.put("name", restaurant.getName());
			response.put("address", restaurant.getAddress());
			response.put("phone", restaurant.getPhone());
			response.put("rating", restaurant.getRating());
			response.put("logoUrl", restaurant.getLogoUrl());
			response.put("openingTime", restaurant.getOpeningTime());
			response.put("closingTime", restaurant.getClosingTime());
			response.put("ownerId", restaurant.getOwner().getUserId());
		} else {
			// If the user is a customer, return limited details
			response.put("name", restaurant.getName());
			response.put("address", restaurant.getAddress());
			response.put("openingTime", restaurant.getOpeningTime());
			response.put("closingTime", restaurant.getClosingTime());
			response.put("rating", restaurant.getRating());
		}

		// Handle null values
		response.replaceAll((key, value) -> value == null ? "N/A" : value);

		return response;
	}

	/**
	 * Updates the restaurant details if the user is authorized to do so.
	 * 
	 * @param id   Restaurant ID
	 * @param dto  DTO containing updated restaurant details
	 * @param user User attempting to update the restaurant
	 * @return Success message
	 */
	public Restaurant getRestaurantEntityById(long id) {
		return restaurantRepository.findById(id).orElseThrow(() -> {
			logger.error("Restaurant with ID {} not found.", id);
			return new RuntimeException("Restaurant not found");
		});
	}

	/**
	 * Updates restaurant details if the requesting user is the owner. Logs
	 * unauthorized access attempts and successful updates.
	 *
	 * @param id   the ID of the restaurant to update.
	 * @param dto  the updated restaurant details.
	 * @param user the user making the update request.
	 * @return Success message if the update is successful.
	 */
	public String updateRestaurant(long id, RestaurantDto dto, User user) {
		logger.info("Received request to update restaurant with ID: {}", id);

		Restaurant restaurant = getRestaurantEntityById(id);

		// Ensure the user is the owner of the restaurant
		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user {} to update restaurant with ID {}", user.getEmail(), id);
			throw new UnauthorizedAccessException("Unauthorized access to update restaurant.");
		}
		logger.info("Updating restaurant '{}' (ID: {}) by owner '{}'.", restaurant.getName(), id, user.getEmail());
		restaurant.setName(dto.getName());
		restaurant.setAddress(dto.getAddress());
		restaurant.setPhone(dto.getPhone());
		restaurant.setLogoUrl(dto.getLogoUrl());

		restaurantRepository.save(restaurant);
		logger.info("Restaurant '{}' updated by owner '{}'.", restaurant.getName(), user.getEmail());
		return "Restaurant updated successfully!";
	}

	/**
	 * Deletes the restaurant if the user is authorized to do so.
	 * 
	 * @param id   Restaurant ID
	 * @param user User attempting to delete the restaurant
	 * @return Success message
	 */
	public String deleteRestaurant(long id, User user) {
		Restaurant restaurant = getRestaurantEntityById(id);

		logger.info("Logged-in User ID: {}, Restaurant Owner ID: {}", user.getUserId(),
				restaurant.getOwner().getUserId());

		// Ensure the user is the owner of the restaurant
		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user {} to delete restaurant with ID {}", user.getEmail(), id);
			throw new UnauthorizedAccessException("Unauthorized access to delete restaurant.");
		}

		restaurantRepository.delete(restaurant);
		logger.info("Restaurant '{}' deleted by owner '{}'.", restaurant.getName(), user.getEmail());
		return "Restaurant deleted successfully!";
	}

	/**
	 * Retrieves a list of all restaurants with selected details. Logs the retrieval
	 * process.
	 *
	 * @return List of maps containing restaurant details.
	 */
	public List<Map<String, Object>> getAllRestaurants() {
		logger.info("Fetching all restaurants from the database.");

		List<Restaurant> restaurants = restaurantRepository.findAll();

		logger.info("Found {} restaurants.", restaurants.size());

		return restaurants.stream().map(restaurant -> {
			Map<String, Object> restaurantData = new HashMap<>();
			restaurantData.put("name", restaurant.getName());
			restaurantData.put("address", restaurant.getAddress());
			restaurantData.put("openingTime", restaurant.getOpeningTime());
			restaurantData.put("closingTime", restaurant.getClosingTime());
			restaurantData.put("rating", restaurant.getRating());

			logger.debug("Mapped restaurant: {}", restaurant.getName());
			return restaurantData;
		}).collect(Collectors.toList());
	}

	public byte[] getMenuItemSalesChart(String email) {
		// Validate email input
		if (email == null) {
			throw new IllegalArgumentException("Email cannot be null");
		}
		if (email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email cannot be empty");
		}

		System.out.println("Executing Query for: " + email);
		List<Object[]> result = orderitemRepository.findSalesStatsByOwner(email);

		if (result.isEmpty()) {
			System.out.println("No data found for: " + email);
		}

		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();

		for (Object[] row : result) {
			labels.add((String) row[0]);
			values.add((Long) row[1]);
		}

		return Charts.generateRestaurantBarChart("Top Selling Items", "Item", "Quantity Sold", labels, values);
	}

}
