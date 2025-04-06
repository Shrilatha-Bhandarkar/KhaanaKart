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

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuItemService {
	private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);

	@Autowired
	private MenuItemRepository menuItemRepository;

	@Autowired
	private MenuCategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Adds a new menu item to the system.
	 * 
	 * @param dto Data transfer object containing menu item details.
	 * @return The created menu item.
	 */
	public MenuItemDto addMenuItem(MenuItemDto dto, String userEmail) {
		logger.info("Adding menu item '{}' to category ID: {}", dto.getName(), dto.getCategoryId());

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Find the menu category and ensure the owner is the logged-in user
		MenuCategory category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		Restaurant restaurant = category.getRestaurant();
		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("User {} is not the owner of restaurant {}", user.getEmail(), restaurant.getRestaurantId());
			throw new UnauthorizedAccessException("You are not authorized to add menu items to this restaurant");
		}

		// Create and save the menu item
		MenuItem item = new MenuItem();
		item.setCategory(category);
		item.setRestaurant(restaurant);
		item.setName(dto.getName());
		item.setDescription(dto.getDescription());
		item.setPrice(dto.getPrice());
		item.setImageUrl(dto.getImageUrl());
		item.setVegetarian(dto.isVegetarian());
		item.setAvailable(dto.isAvailable());
		item.setPreparationTimeMin(dto.getPreparationTimeMin());

		item = menuItemRepository.save(item);

		dto.setItemId(item.getItemId());
		logger.info("Successfully added menu item '{}'.", dto.getName());
		return dto;
	}

	/**
	 * Retrieves a menu item by its ID.
	 * 
	 * @param itemId ID of the menu item.
	 * @return The menu item details.
	 */
	public MenuItemDto getMenuItemById(long itemId) {
		logger.info("Fetching menu item with ID: {}", itemId);
		MenuItem item = menuItemRepository.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("Menu Item not found"));

		return new MenuItemDto(item.getItemId(), item.getCategory().getCategoryId(),
				item.getRestaurant().getRestaurantId(), item.getName(), item.getDescription(), item.getPrice(),
				item.getImageUrl(), item.isVegetarian(), item.isAvailable(), item.getPreparationTimeMin());
	}

	/**
	 * Retrieves all menu items for a specific category within a given restaurant.
	 *
	 * @param restaurantId The ID of the restaurant.
	 * @param categoryId   The ID of the menu category.
	 * @return A list of menu items belonging to the specified category.
	 * @throws ResourceNotFoundException if the category is not found.
	 * @throws IllegalArgumentException  if the category does not belong to the
	 *                                   specified restaurant.
	 */

	public List<MenuItemDto> getAllMenuItemsForCategory(Long restaurantId, Long categoryId) {
		logger.info("Fetching all menu items for category ID: {} in restaurant ID: {}", categoryId, restaurantId);

		// Ensure the category belongs to the given restaurant
		MenuCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> {
			logger.error("Category with ID {} not found", categoryId);
			return new ResourceNotFoundException("Category not found");
		});

		if (category.getRestaurant().getRestaurantId() != restaurantId) {
			logger.error("Category ID {} does not belong to restaurant ID {}", categoryId, restaurantId);
			throw new IllegalArgumentException("Category does not belong to the specified restaurant");
		}

		return menuItemRepository.findByCategoryCategoryId(categoryId).stream()
				.map(menuItem -> new MenuItemDto(menuItem.getItemId(), menuItem.getCategory().getCategoryId(),
						menuItem.getRestaurant().getRestaurantId(), menuItem.getName(), menuItem.getDescription(),
						menuItem.getPrice(), menuItem.getImageUrl(), menuItem.isVegetarian(), menuItem.isAvailable(),
						menuItem.getPreparationTimeMin()))
				.collect(Collectors.toList());
	}

	/**
	 * Updates an existing menu item.
	 * 
	 * @param itemId ID of the menu item.
	 * @param dto    Updated menu item details.
	 * @return The updated menu item.
	 */
	public MenuItemDto updateMenuItem(Long itemId, MenuItemDto dto, String userEmail) {
		logger.info("Updating menu item with ID: {}", itemId);

		MenuItem menuItem = menuItemRepository.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

		// Ensure the logged-in user is the owner of the restaurant
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = menuItem.getCategory().getRestaurant();
		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user {} to update item ID {}", user.getEmail(), itemId);
			throw new UnauthorizedAccessException("You are not the owner of this restaurant.");
		}

		// Ensure the restaurant in DTO matches the menu item's restaurant
		if (menuItem.getRestaurant().getRestaurantId() != dto.getRestaurantId()) {
			throw new ResourceNotFoundException("Restaurant mismatch");
		}

		MenuCategory category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		menuItem.setCategory(category);
		menuItem.setName(dto.getName());
		menuItem.setDescription(dto.getDescription());
		menuItem.setPrice(dto.getPrice());
		menuItem.setImageUrl(dto.getImageUrl());
		menuItem.setVegetarian(dto.isVegetarian());
		menuItem.setAvailable(dto.isAvailable());
		menuItem.setPreparationTimeMin(dto.getPreparationTimeMin());

		menuItem = menuItemRepository.save(menuItem);
		dto.setItemId(menuItem.getItemId());
		return dto;
	}

	/**
	 * Deletes a menu item from the system.
	 * 
	 * @param itemId ID of the menu item to be deleted.
	 */
	public String deleteMenuItem(Long itemId, String userEmail) {
		MenuItem menuItem = menuItemRepository.findById(itemId)
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = menuItem.getCategory().getRestaurant();

		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user {} to delete item ID {}", user.getEmail(), itemId);
			throw new UnauthorizedAccessException("You are not the owner of this restaurant.");
		}

		menuItemRepository.delete(menuItem);
		logger.info("Menu Item '{}' deleted by owner '{}'.", menuItem.getName(), user.getEmail());
		return "Menu item deleted successfully!";
	}
}