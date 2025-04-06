package com.onlinefoodorder.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlinefoodorder.dto.CartDto;
import com.onlinefoodorder.entity.CartItem;
import com.onlinefoodorder.service.CartService;

/**
 * Controller for handling cart-related operations
 */
@RestController
@RequestMapping("/customer/cart")
public class CartController {
	@Autowired
	private CartService cartService;
	private static final Logger logger = LoggerFactory.getLogger(CartController.class);

	/**
	 * Adds an item to the user's cart.
	 * 
	 * @param cartDto   Contains the menu item ID and quantity.
	 * @param principal The authenticated user.
	 * @return ResponseEntity with success or failure message.
	 */
	@PostMapping("/add")
	public ResponseEntity<String> addToCart(@RequestBody CartDto cartDto, Principal principal) {
		try {
			logger.info("User {} is adding item to cart", principal.getName());
			cartService.addToCart(principal.getName(), cartDto);
			logger.info("Item added to cart successfully for user {}", principal.getName());
			return ResponseEntity.ok("Item added to cart");
		} catch (Exception e) {
			logger.error("Error adding item to cart for user {}: {}", principal.getName(), e.getMessage());
			return ResponseEntity.status(500).body("Error adding item to cart");
		}
	}

	/**
	 * Retrieves the cart items for the authenticated user.
	 * 
	 * @param principal The authenticated user.
	 * @return ResponseEntity containing the user's cart items.
	 */
	@GetMapping
	public ResponseEntity<List<CartItem>> getCart(Principal principal) {
		try {
			String userEmail = principal.getName();
			logger.info("Fetching cart for user {}", userEmail);
			List<CartItem> cartItems = cartService.getCart(userEmail);
			logger.info("Cart fetched successfully for user {}", userEmail);
			return ResponseEntity.ok(cartItems);
		} catch (Exception e) {
			logger.error("Error fetching cart for user {}: {}", principal.getName(), e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * Updates the quantity of a specific cart item.
	 * 
	 * @param cartId    The ID of the cart item to update.
	 * @param cartDto   Contains the updated quantity.
	 * @param principal The authenticated user.
	 * @return ResponseEntity with success or failure message.
	 */
	@PutMapping("/update/{cartId}")
	public ResponseEntity<String> updateCartItem(@PathVariable Long cartId, @RequestBody CartDto cartDto,
			Principal principal) {
		try {
			logger.info("Updating cart item {} for user {}", cartId, principal.getName());
			cartService.updateCartItem(cartId, cartDto.getQuantity(), principal.getName());
			logger.info("Cart item {} updated successfully", cartId);
			return ResponseEntity.ok("Cart item updated");
		} catch (Exception e) {
			logger.error("Error updating cart item {}: {}", cartId, e.getMessage());
			return ResponseEntity.status(500).body("Error updating cart item");
		}
	}

	/**
	 * Removes an item from the cart.
	 * 
	 * @param cartId The ID of the cart item to remove.
	 * @return ResponseEntity with success or failure message.
	 */
	@DeleteMapping("/{cartId}")
	public ResponseEntity<String> removeFromCart(@PathVariable Long cartId, Principal principal) {
		try {
			String userEmail = principal.getName();
			logger.info("Removing item with ID {} from cart for user {}", cartId, userEmail);
			cartService.removeFromCart(cartId, userEmail);
			logger.info("Item with ID {} removed from cart for user {}", cartId, userEmail);
			return ResponseEntity.ok("Item removed from cart");
		} catch (Exception e) {
			logger.error("Error removing item with ID {} from cart: {}", cartId, e.getMessage());
			return ResponseEntity.status(500).body("Error removing item from cart");
		}
	}

}