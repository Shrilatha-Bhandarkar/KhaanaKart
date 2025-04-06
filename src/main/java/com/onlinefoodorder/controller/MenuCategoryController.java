package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.MenuCategoryDto;
import com.onlinefoodorder.service.MenuCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling menu category operations, such as adding, updating,
 * retrieving, and deleting categories for a restaurant.
 */
@RestController
@RequestMapping("/restaurant/menu-category")
public class MenuCategoryController {

	private static final Logger logger = LoggerFactory.getLogger(MenuCategoryController.class);

	@Autowired
	private MenuCategoryService categoryService;

	/**
	 * Add a new menu category (Only Restaurant Owners).
	 *
	 * @param dto       The category data transfer object.
	 * @param principal The authenticated user.
	 * @return A success message.
	 */
	@PostMapping
	@Transactional
	public ResponseEntity<String> addCategory(@RequestBody MenuCategoryDto dto, Principal principal) {
		String loggedInUser = principal.getName();
		logger.info("User '{}' is adding new category '{}' for restaurant ID: {}", loggedInUser, dto.getName(),
				dto.getRestaurantId());

		String responseMessage = categoryService.createMenuCategory(dto, loggedInUser);
		return ResponseEntity.ok(responseMessage);
	}

	/**
	 * Get a specific menu category by ID for a given restaurant.
	 *
	 * @param restaurantId The restaurant ID.
	 * @param categoryId   The category ID.
	 * @return The menu category data.
	 */
	@GetMapping("/{restaurantId}/{categoryId}")
	public ResponseEntity<MenuCategoryDto> getCategoryById(@PathVariable Long restaurantId,
			@PathVariable Long categoryId) {
		logger.info("Fetching category ID: {} for restaurant ID: {}", categoryId, restaurantId);
		MenuCategoryDto category = categoryService.getCategoryById(restaurantId, categoryId);
		return ResponseEntity.ok(category);
	}

	/**
	 * Get all menu categories for a restaurant.
	 *
	 * @param restaurantId The restaurant ID.
	 * @return A list of menu categories.
	 */
	@GetMapping("/{restaurantId}/all")
	public ResponseEntity<List<MenuCategoryDto>> getAllCategories(@PathVariable Long restaurantId) {
		logger.info("Fetching all menu categories for restaurant ID: {}", restaurantId);
		List<MenuCategoryDto> categories = categoryService.getAllCategoriesForRestaurant(restaurantId);
		return ResponseEntity.ok(categories);
	}

	/**
	 * Update an existing menu category (Only Restaurant Owners).
	 *
	 * @param categoryId The category ID.
	 * @param dto        The updated category data.
	 * @param principal  The authenticated user.
	 * @return A success message.
	 */
	@PutMapping("/{categoryId}")
	@Transactional
	public ResponseEntity<String> updateMenuCategory(@PathVariable Long categoryId, @RequestBody MenuCategoryDto dto,
			Principal principal) {
		String loggedInUser = principal.getName();
		logger.info("User '{}' is updating category ID: {}", loggedInUser, categoryId);

		String responseMessage = categoryService.updateCategory(categoryId, dto, loggedInUser);
		return ResponseEntity.ok(responseMessage);
	}

	/**
	 * Delete a menu category by its ID (Only Restaurant Owners).
	 *
	 * @param categoryId The category ID.
	 * @param principal  The authenticated user.
	 * @return A success message.
	 */
	@DeleteMapping("/{categoryId}")
	@Transactional
	public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId, Principal principal) {
		String loggedInUser = principal.getName();
		logger.info("User '{}' is deleting category ID: {}", loggedInUser, categoryId);

		String responseMessage = categoryService.deleteCategory(categoryId, loggedInUser);
		return ResponseEntity.ok(responseMessage);
	}
}
