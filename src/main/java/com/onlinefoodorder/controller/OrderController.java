package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.exception.OrderNotFoundException;
import com.onlinefoodorder.service.OrderService;
import com.onlinefoodorder.util.Status.OrderStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling order-related operations, such as placing orders,
 * updating statuses, and applying coupons.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private OrderService orderService;

	/**
	 * Places a new order for the logged-in user.
	 * 
	 * @param principal The currently logged-in user.
	 * @param orderDto  Order details to be placed.
	 * @return The placed order details.
	 */
	@PostMapping("/place")
	@Transactional
	public ResponseEntity<OrderDto> placeOrder(Principal principal, @RequestBody OrderDto orderDto) {
		String userEmail = principal.getName();
		logger.info("User '{}' is placing an order.", userEmail);

		OrderDto placedOrder = orderService.placeOrder(userEmail, orderDto);
		return ResponseEntity.ok(placedOrder);
	}

	/**
	 * Retrieves an order by its ID.
	 * 
	 * @param orderId The ID of the order.
	 * @return The order details.
	 */
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
		logger.info("Fetching order with ID: {}", orderId);

		OrderDto order = orderService.getOrderById(orderId);
		if (order == null) {
			throw new OrderNotFoundException("Order not found with ID: " + orderId);
		}

		return ResponseEntity.ok(order);
	}

	/**
	 * Retrieves all orders for the logged-in user.
	 * 
	 * @param principal The currently logged-in user.
	 * @return List of orders associated with the user.
	 */
	@GetMapping("/user")
	public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {
		String userEmail = principal.getName();
		logger.info("Fetching orders for user '{}'", userEmail);

		List<OrderDto> orders = orderService.getUserOrders(userEmail);
		return ResponseEntity.ok(orders);
	}

	/**
	 * Updates the status of an order.
	 * 
	 * @param orderId   The ID of the order to update.
	 * @param request   A map containing the new order status.
	 * @param principal The currently logged-in user.
	 * @return The updated order details.
	 */
	@PutMapping("/status/{orderId}")
	@Transactional
	public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long orderId,
			@RequestBody Map<String, String> request, Principal principal) {

		String userEmail = principal.getName();
		OrderStatus orderStatus = OrderStatus.valueOf(request.get("status").toUpperCase());
		logger.info("User '{}' is updating order ID {} to status '{}'", userEmail, orderId, orderStatus);

		OrderDto updatedOrder = orderService.updateOrderStatus(orderId, orderStatus, userEmail);
		return ResponseEntity.ok(updatedOrder);
	}

	/**
	 * Applies a coupon to an order.
	 * 
	 * @param requestBody A map containing order ID and coupon code.
	 * @return The updated order details with the applied coupon.
	 */
	@PostMapping("/coupons/apply")
	@Transactional
	public ResponseEntity<OrderDto> applyCouponToOrder(@RequestBody Map<String, Object> requestBody) {
		Long orderId = ((Number) requestBody.get("orderId")).longValue();
		String couponCode = (String) requestBody.get("couponCode");

		logger.info("Applying coupon '{}' to order ID {}", couponCode, orderId);
		return ResponseEntity.ok(orderService.applyCoupon(orderId, couponCode));
	}

	/**
	 * Removes a coupon from an order.
	 * 
	 * @param orderId The ID of the order from which to remove the coupon.
	 * @return A success message.
	 */
	@DeleteMapping("/coupons/remove/{orderId}")
	@Transactional
	public ResponseEntity<String> removeCouponFromOrder(@PathVariable Long orderId) {
		logger.info("Removing coupon from order ID {}", orderId);
		orderService.removeCoupon(orderId);
		return ResponseEntity.ok("Coupon removed successfully.");
	}
}
