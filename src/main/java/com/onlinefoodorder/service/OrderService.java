package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.OrderDto;

import com.onlinefoodorder.dto.OrderItemDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.exception.OrderNotFoundException;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.Status.DiscountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling order-related operations, such as placing orders,
 * updating statuses, applying coupons, and retrieving order details.
 */

@Service
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private CustomerAddressRepository addressRepository;

	@Autowired
	private MenuItemRepository menuItemRepository;

	@Autowired
	private CouponRepository couponRepository;

	/**
	 * Places a new order for a user.
	 * 
	 * @param userEmail The email of the user placing the order.
	 * @param orderDto  The order details.
	 * @return The placed order as a DTO.
	 */
	@Transactional
	public OrderDto placeOrder(String userEmail, OrderDto orderDto) {
		// Validate input amounts first
		if (orderDto.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Subtotal must be positive");
		}
		if (orderDto.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Delivery fee cannot be negative");
		}
		if (orderDto.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Tax cannot be negative");
		}

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Restaurant restaurant = restaurantRepository.findById(orderDto.getRestaurantId())
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

		CustomerAddress deliveryAddress = addressRepository.findById(orderDto.getDeliveryAddressId())
				.orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));

		Order order = new Order();
		order.setUser(user);
		order.setRestaurant(restaurant);
		order.setDeliveryAddress(deliveryAddress);
		order.setStatus(OrderStatus.CONFIRMED); // ✅ Change this line to PLACED
		order.setSpecialInstructions(orderDto.getSpecialInstructions());
		order.setCreatedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());
		List<OrderItem> orderItems = orderDto.getOrderItems().stream().map(itemDto -> {
			MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

			if (menuItem.getRestaurant().getRestaurantId() != orderDto.getRestaurantId()) {
				throw new IllegalArgumentException(
						"Menu item " + menuItem.getItemId() + " does not belong to the selected restaurant");
			}

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setMenuItem(menuItem);
			orderItem.setQuantity(itemDto.getQuantity());
			orderItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
			return orderItem;
		}).collect(Collectors.toList());

		order.setOrderItems(orderItems);
		order.setTotalAmount(orderItems.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
		// Validate totalAmount
		if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Total amount must be greater than zero");
		}
		order.setTaxAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(0.05))); // 5% tax
		order.setDeliveryFee(BigDecimal.valueOf(50)); // Flat delivery fee
		order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30)); // Default ETA 30 min

		if (orderDto.getCouponCode() != null) {
			applyCouponToOrder(order, orderDto.getCouponCode());
		}

		Order savedOrder = orderRepository.save(order);
		return mapToDto(savedOrder);
	}

	/**
	 * Apply Coupon to an order for a user.
	 * 
	 * @param order      The order details.
	 * @param couponCode The code of the coupon to apply
	 */
	private void applyCouponToOrder(Order order, String couponCode) {
		Coupon coupon = couponRepository.findByCode(couponCode)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code"));
		if (order.getCoupon() != null) {
			throw new IllegalStateException("A coupon is already applied to this order.");
		}
		// Ensure coupon belongs to the restaurant
		if (coupon.getRestaurant().getRestaurantId() != order.getRestaurant().getRestaurantId()) {
			throw new IllegalArgumentException("This coupon is not valid for the selected restaurant.");
		}

		// Check coupon validity
		if (!coupon.isActive() || LocalDateTime.now().isBefore(coupon.getValidFrom())
				|| LocalDateTime.now().isAfter(coupon.getValidTo())) {
			throw new IllegalArgumentException("This coupon is not valid at this time.");
		}

		// Ensure the order meets the minimum order value
		if (order.getTotalAmount().compareTo(coupon.getMinOrderValue()) < 0) {
			throw new IllegalArgumentException("Order total is less than the minimum required to use this coupon.");
		}

		// Calculate discount
		BigDecimal discountAmount = calculateDiscount(order.getTotalAmount(), coupon);
		order.setTotalAmount(order.getTotalAmount().subtract(discountAmount));
		order.setCoupon(coupon);
		order.setDiscountAmount(discountAmount);
	}

	/**
	 * Retrieves an order by its ID.
	 * 
	 * @param orderId The ID of the order.
	 * @return The order details as a DTO.
	 */
	public OrderDto getOrderById(Long orderId) {
		logger.info("Fetching order with ID: {}", orderId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
		return mapToDto(order);
	}

	/**
	 * Retrieves all orders for a specific user.
	 * 
	 * @param userEmail The email of the user.
	 * @return A list of orders.
	 */
	public List<OrderDto> getUserOrders(String userEmail) {
		logger.info("Fetching orders for user '{}'", userEmail);

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return orderRepository.findByUser(user).stream().map(this::mapToDto).collect(Collectors.toList());
	}

	/**
	 * Updates the status of an order.
	 * 
	 * @param orderId   The ID of the order.
	 * @param status    The new status.
	 * @param userEmail The email of the user performing the update.
	 * @return The updated order as a DTO.
	 */
	@Transactional
	public OrderDto updateOrderStatus(Long orderId, OrderStatus status, String userEmail) {
		logger.info("User '{}' updating order ID {} to status '{}'", userEmail, orderId, status);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found"));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (status == OrderStatus.PREPARING || status == OrderStatus.READY_FOR_PICKUP) {
			if (!order.getRestaurant().getOwner().getUserId().equals(user.getUserId())) {
				throw new UnauthorizedAccessException("Only the restaurant owner can update this status.");
			}
		} else if (status == OrderStatus.OUT_FOR_DELIVERY || status == OrderStatus.DELIVERED) {
			if (order.getDeliveryPerson() == null || !order.getDeliveryPerson().getUserId().equals(user.getUserId())) {
				throw new UnauthorizedAccessException("Only the assigned delivery person can update this status.");
			}
		}

		order.setStatus(status);
		order.setUpdatedAt(LocalDateTime.now());
		Order updatedOrder = orderRepository.save(order);

		return mapToDto(updatedOrder);
	}

	/**
	 * Applies a coupon to an order if no coupon is already applied.
	 *
	 * @param orderId    The ID of the order.
	 * @param couponCode The coupon code to apply.
	 * @return The updated order details with the applied discount.
	 * @throws ResourceNotFoundException if the order is not found.
	 * @throws IllegalStateException     if a coupon is already applied.
	 */
	@Transactional
	public OrderDto applyCoupon(Long orderId, String couponCode) {
		logger.info("Applying coupon '{}' to order ID: {}", couponCode, orderId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

		if (order.getCoupon() != null) {
			logger.warn("Coupon '{}' is already applied to order ID: {}", order.getCoupon().getCode(), orderId);
			throw new IllegalStateException("A coupon is already applied to this order.");
		}

		applyCouponToOrder(order, couponCode);
		orderRepository.save(order);

		logger.info("Coupon '{}' successfully applied to order ID: {}", couponCode, orderId);
		return mapToDto(order);
	}

	/**
	 * Removes the applied coupon from an order and reverts the discount.
	 *
	 * @param orderId The ID of the order.
	 * @return The updated order details after removing the coupon.
	 * @throws ResourceNotFoundException if the order is not found.
	 * @throws IllegalStateException     if no coupon is applied to the order.
	 */
	@Transactional
	public OrderDto removeCoupon(Long orderId) {
		logger.info("Attempting to remove coupon from order ID: {}", orderId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

		if (order.getCoupon() == null) {
			logger.warn("No coupon found for order ID: {}", orderId);
			throw new IllegalStateException("No coupon applied to this order.");
		}

		// Revert discount
		order.setTotalAmount(order.getTotalAmount().add(order.getDiscountAmount()));
		order.setCoupon(null);
		order.setDiscountAmount(BigDecimal.ZERO);

		orderRepository.save(order);

		logger.info("Coupon successfully removed from order ID: {}", orderId);
		return mapToDto(order);
	}

	/**
	 * Calculates the discount amount based on the given coupon.
	 * 
	 * @param totalAmount The total order amount before applying the discount.
	 * @param coupon      The coupon to be applied.
	 * @return The discounted amount, ensuring it does not exceed the max discount.
	 */
	private BigDecimal calculateDiscount(BigDecimal totalAmount, Coupon coupon) {
		BigDecimal discountAmount;

		if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
			// Calculate percentage-based discount
			discountAmount = totalAmount
					.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
		} else {
			// Fixed amount discount
			discountAmount = coupon.getDiscountValue();
		}

		// Ensure the discount does not exceed the maximum allowed limit
		return discountAmount.min(coupon.getMaxDiscount());
	}

	/**
	 * Maps an Order entity to an OrderDto. Ensures null safety for nested objects
	 * to prevent NullPointerException.
	 *
	 * @param order the Order entity to map.
	 * @return an OrderDto containing the mapped fields.
	 */
	private OrderDto mapToDto(Order order) {
		return new OrderDto(order.getOrderId(), order.getUser().getUserId(), order.getRestaurant().getRestaurantId(),
				order.getDeliveryAddress().getAddressId(),
				order.getDeliveryPerson() != null ? order.getDeliveryPerson().getUserId() : null,
				order.getOrderItems().stream().map(this::mapToOrderItemDto).collect(Collectors.toList()),
				order.getTotalAmount(), order.getDeliveryFee(), order.getTaxAmount(), order.getSpecialInstructions(),
				order.getEstimatedDeliveryTime(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt(),
				order.getPayment(), order.getCoupon() != null ? order.getCoupon().getCode() : null, // ✅ Added
																									// couponCode
				order.getDiscountAmount() // ✅ Added discountAmount
		);
	}

	/**
	 * Maps an OrderItem entity to an OrderItemDto. Ensures null safety for
	 * associated entities to avoid NullPointerException.
	 *
	 * @param orderItem the OrderItem entity to map.
	 * @return an OrderItemDto containing the mapped fields.
	 */
	private OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
		return new OrderItemDto(orderItem.getOrderItemId(), orderItem.getOrder().getOrderId(),
				orderItem.getMenuItem().getItemId(), orderItem.getQuantity(), orderItem.getPrice(),
				orderItem.getSpecialRequest());
	}
}
