package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.MenuItemDto;
import com.onlinefoodorder.service.MenuItemService;
import com.onlinefoodorder.exception.ResourceNotFoundException;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling menu item operations, such as adding, updating,
 * retrieving, and deleting items for a restaurant.
 */
@RestController
@RequestMapping("/restaurant/menu-item")
public class MenuItemController {

	private static final Logger logger = LoggerFactory.getLogger(MenuItemController.class);

	@Autowired
	private MenuItemService menuItemService;

	/**
	 * Endpoint to add a new menu item.
	 * 
	 * @param dto Menu item data transfer object.
	 * @return The created menu item.
	 */

	@PostMapping
	public ResponseEntity<MenuItemDto> addMenuItem(@RequestBody MenuItemDto dto, Principal principal) {
		String userEmail = principal.getName();
		logger.info("Adding new menu item: {} by user: {}", dto.getName(), userEmail);
		return ResponseEntity.ok(menuItemService.addMenuItem(dto, userEmail));
	}

	/**
	 * Endpoint to fetch a menu item by its ID.
	 * 
	 * @param itemId ID of the menu item.
	 * @return The menu item details.
	 */
	@GetMapping("/{itemId}")
	public ResponseEntity<MenuItemDto> getMenuItemById(@PathVariable Long itemId) {
		logger.info("Fetching menu item with ID: {}", itemId);
		return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
	}

	@GetMapping("/{restaurantId}/{categoryId}/all")
	public List<MenuItemDto> getAllMenuItemsForCategory(@PathVariable Long restaurantId,
			@PathVariable Long categoryId) {
		logger.info("Fetching all menu items for category ID {} in restaurant ID {}", categoryId, restaurantId);
		return menuItemService.getAllMenuItemsForCategory(restaurantId, categoryId);
	}

	/**
	 * Endpoint to update an existing menu item.
	 * 
	 * @param itemId ID of the menu item.
	 * @param dto    Updated menu item details.
	 * @return The updated menu item.
	 */
	@PutMapping("/{itemId}")
	public ResponseEntity<MenuItemDto> updateMenuItem(@PathVariable Long itemId, @RequestBody MenuItemDto dto,
			Principal principal) {
		String userEmail = principal.getName();
		logger.info("Updating menu item with ID: {} by user: {}", itemId, userEmail);
		return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, dto, userEmail));
	}

	/**
	 * Endpoint to delete a menu item.
	 * 
	 * @param itemId ID of the menu item.
	 * @return Success message.
	 */
	@DeleteMapping("/{itemId}")
	public ResponseEntity<String> deleteMenuItem(@PathVariable Long itemId, Principal principal) {
		String userEmail = principal.getName();
		logger.warn("Deleting menu item with ID: {} by user: {}", itemId, userEmail);
		return ResponseEntity.ok(menuItemService.deleteMenuItem(itemId, userEmail));
	}
}