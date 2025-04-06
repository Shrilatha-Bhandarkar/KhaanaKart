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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing menu categories, including creation, retrieval,
 * updating, and deletion, ensuring proper authorization and logging.
 */
@Service
public class MenuCategoryService {

	private static final Logger logger = LoggerFactory.getLogger(MenuCategoryService.class);

	@Autowired
	private MenuCategoryRepository categoryRepository;

	@Autowired
	private MenuItemRepository menuItemRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Creates a new menu category for a restaurant (Only Restaurant Owners
	 * Allowed).
	 *
	 * @param dto       The category details.
	 * @param userEmail The authenticated user's email.
	 * @return Success message.
	 */
	@Transactional
	public String createMenuCategory(MenuCategoryDto dto, String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user '{}' to add category to restaurant ID: {}", user.getEmail(),
					restaurant.getRestaurantId());
			throw new UnauthorizedAccessException("You are not the owner of this restaurant.");
		}

		MenuCategory category = new MenuCategory();
		category.setName(dto.getName());
		category.setDescription(dto.getDescription());
		category.setRestaurant(restaurant);

		categoryRepository.save(category);

		logger.info("Menu Category '{}' created by owner '{}' for restaurant '{}'.", category.getName(),
				user.getEmail(), restaurant.getName());
		return "Menu category created successfully!";
	}

	/**
	 * Retrieves a menu category by ID, ensuring it belongs to the given restaurant.
	 *
	 * @param restaurantId The restaurant ID.
	 * @param categoryId   The category ID.
	 * @return The menu category details.
	 */
	public MenuCategoryDto getCategoryById(Long restaurantId, Long categoryId) {
		logger.info("Fetching category ID: {} for restaurant ID: {}", categoryId, restaurantId);

		MenuCategory category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (category.getRestaurant().getRestaurantId() != restaurantId) {
			throw new ResourceNotFoundException("Category not found for this restaurant.");
		}

		long itemCount = menuItemRepository.countByCategoryCategoryId(categoryId);
		return new MenuCategoryDto(category.getCategoryId(), restaurantId, category.getName(),
				category.getDescription(), itemCount);
	}

	/**
	 * Retrieves all menu categories for a given restaurant.
	 *
	 * @param restaurantId The restaurant ID.
	 * @return A list of menu categories.
	 */
	public List<MenuCategoryDto> getAllCategoriesForRestaurant(Long restaurantId) {
		logger.info("Fetching all categories for restaurant ID: {}", restaurantId);

		return categoryRepository.findByRestaurantRestaurantId(restaurantId).stream()
				.map(category -> new MenuCategoryDto(category.getCategoryId(), restaurantId, category.getName(),
						category.getDescription(),
						menuItemRepository.countByCategoryCategoryId(category.getCategoryId())))
				.collect(Collectors.toList());
	}

	/**
	 * Updates an existing menu category (Only Restaurant Owners Allowed).
	 *
	 * @param categoryId The category ID.
	 * @param dto        The updated category details.
	 * @param userEmail  The authenticated user's email.
	 * @return Success message.
	 */
	@Transactional
	public String updateCategory(Long categoryId, MenuCategoryDto dto, String userEmail) {
		MenuCategory category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = category.getRestaurant();

		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user '{}' to update category ID: {}", user.getEmail(), categoryId);
			throw new UnauthorizedAccessException("You are not the owner of this restaurant.");
		}

		category.setName(dto.getName());
		category.setDescription(dto.getDescription());
		categoryRepository.save(category);

		logger.info("Category '{}' updated by owner '{}'.", category.getName(), user.getEmail());
		return "Category updated successfully!";
	}

	/**
	 * Deletes a menu category (Only Restaurant Owners Allowed).
	 *
	 * @param categoryId The category ID.
	 * @param userEmail  The authenticated user's email.
	 * @return Success message.
	 */
	@Transactional
	public String deleteCategory(Long categoryId, String userEmail) {
		MenuCategory category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = category.getRestaurant();

		if (!restaurant.getOwner().getUserId().equals(user.getUserId())) {
			logger.error("Unauthorized attempt by user '{}' to delete category ID: {}", user.getEmail(), categoryId);
			throw new UnauthorizedAccessException("You are not the owner of this restaurant.");
		}

		categoryRepository.delete(category);
		logger.info("Category '{}' deleted by owner '{}'.", category.getName(), user.getEmail());
		return "Category deleted successfully!";
	}
}
