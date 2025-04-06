package com.onlinefoodorder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinefoodorder.dto.CartDto;
import com.onlinefoodorder.entity.Cart;
import com.onlinefoodorder.entity.CartItem;
import com.onlinefoodorder.entity.MenuItem;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.CartItemRepository;
import com.onlinefoodorder.repository.CartRepository;
import com.onlinefoodorder.repository.MenuItemRepository;

/**
 * Service for handling cart logic
 */
@Service
public class CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private MenuItemRepository menuItemRepository;

	private static final Logger logger = LoggerFactory.getLogger(CartService.class);

	/**
	 * Adds or updates a menu item in the authenticated user's cart.
	 * 
	 * @param userEmail The email of the authenticated user.
	 * @param cartDto   The DTO containing the menu item details and quantity.
	 */
	@Transactional
	public void addToCart(String userEmail, CartDto cartDto) {
		if (cartDto.getMenuItemId() == null) {
			throw new IllegalArgumentException("Menu item ID cannot be null");
		}
		if (cartDto.getQuantity() <= 0) {
			throw new IllegalArgumentException("Quantity must be positive");
		}

		logger.info("Adding/updating item {} in cart for user {}", cartDto.getMenuItemId(), userEmail);

		User user = userService.getUserByEmail(userEmail);
		MenuItem menuItem = menuItemRepository.findById(cartDto.getMenuItemId())
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

		Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});

		Optional<CartItem> existingCartItem = cart.getCartItems().stream()
				.filter(item -> item.getMenuItem().getItemId() == menuItem.getItemId()).findFirst();

		if (existingCartItem.isPresent()) {
			CartItem cartItem = existingCartItem.get();
			cartItem.setQuantity(cartDto.getQuantity());
			cartItem.setAddedAt(LocalDateTime.now());
		} else {
			CartItem newCartItem = new CartItem();
			newCartItem.setCart(cart);
			newCartItem.setMenuItem(menuItem);
			newCartItem.setQuantity(cartDto.getQuantity());
			newCartItem.setAddedAt(LocalDateTime.now());
			cart.getCartItems().add(newCartItem);
		}

		cartRepository.save(cart);
	}

	/**
	 * Retrieves the cart for the authenticated user.
	 * 
	 * @param userEmail The email of the authenticated user.
	 * @return A list of CartItems.
	 */
	public List<CartItem> getCart(String userEmail) {
		logger.info("Fetching cart for user {}", userEmail);
		User user = userService.getUserByEmail(userEmail);
		Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
		return cart.getCartItems();
	}

	/**
	 * Removes an item from the user's cart.
	 * 
	 * @param cartItemId The ID of the cart item to remove.
	 * @param userEmail  The email of the authenticated user.
	 */
	@Transactional
	public void removeFromCart(Long cartItemId, String userEmail) {
		logger.info("Removing cart item {} for user {}", cartItemId, userEmail);
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

		if (!cartItem.getCart().getUser().getEmail().equals(userEmail)) {
			throw new UnauthorizedAccessException("Unauthorized to remove this cart item");
		}
		cartItemRepository.delete(cartItem);
	}

	/**
	 * Updates the quantity of a cart item.
	 * 
	 * @param cartItemId  The ID of the cart item to update.
	 * @param newQuantity The new quantity of the item.
	 * @param userEmail   The email of the authenticated user.
	 */
	@Transactional
	public void updateCartItem(Long cartItemId, int newQuantity, String userEmail) {
		logger.info("Updating cart item {} for user {}", cartItemId, userEmail);
		CartItem cartItem = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

		if (!cartItem.getCart().getUser().getEmail().equals(userEmail)) {
			throw new UnauthorizedAccessException("Unauthorized to update this cart item");
		}

		if (newQuantity <= 0) {
			cartItemRepository.delete(cartItem);
		} else {
			cartItem.setQuantity(newQuantity);
			cartItemRepository.save(cartItem);
		}
	}

	/**
	 * Clears the cart for the authenticated user.
	 * 
	 * @param userEmail The email of the authenticated user.
	 */
	@Transactional
	public void clearCart(String userEmail) {
		logger.info("Clearing cart for user {}", userEmail);
		User user = userService.getUserByEmail(userEmail);
		Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
		cartItemRepository.deleteAll(cart.getCartItems());
	}

	/**
	 * Calculates the total price of items in the cart.
	 * 
	 * @param userEmail The email of the authenticated user.
	 * @return The total price of the cart.
	 */
	public BigDecimal calculateTotal(String userEmail) {
		User user = userService.getUserByEmail(userEmail);
		Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

		return cart.getCartItems().stream()
				.map(item -> item.getMenuItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
