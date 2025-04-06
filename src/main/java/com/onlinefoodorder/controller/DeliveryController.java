package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.service.DeliveryService;
import com.onlinefoodorder.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling delivery-related operations, such as fetching
 * assigned orders and updating order statuses.
 */
@RestController
@RequestMapping("/delivery/orders")
public class DeliveryController {

	private static final Logger logger = LoggerFactory.getLogger(DeliveryController.class);

	@Autowired
	private DeliveryService deliveryService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Get all assigned deliveries for the logged-in delivery person.
	 *
	 * @param principal The authenticated user's principal.
	 * @return A list of assigned orders.
	 */
	@GetMapping("/assigned")
	public ResponseEntity<List<OrderDto>> getAssignedOrders(Principal principal) {
		Long deliveryPersonId = userService.getUserIdFromPrincipal(principal);
		logger.info("Fetching assigned deliveries for delivery person ID: {}", deliveryPersonId);
		return ResponseEntity.ok(deliveryService.getAssignedOrders(deliveryPersonId));
	}

	/**
	 * Mark an order as "Out for Delivery".
	 *
	 * @param orderId   The order ID.
	 * @param principal The authenticated user's principal.
	 * @return A success message.
	 */
	@PutMapping("/{orderId}/out-for-delivery")
	@Transactional
	public ResponseEntity<String> markOrderOutForDelivery(@PathVariable Long orderId, Principal principal) {
		Long deliveryPersonId = userService.getUserIdFromPrincipal(principal);
		logger.info("Marking order {} as 'Out for Delivery' by delivery person ID: {}", orderId, deliveryPersonId);
		deliveryService.markOrderOutForDelivery(orderId, deliveryPersonId);
		return ResponseEntity.ok("Order marked as Out for Delivery");
	}

	/**
	 * Mark an order as "Delivered".
	 *
	 * @param orderId   The order ID.
	 * @param principal The authenticated user's principal.
	 * @return A success message.
	 */
	@PutMapping("/{orderId}/delivered")
	@Transactional
	public ResponseEntity<String> markOrderDelivered(@PathVariable Long orderId, Principal principal) {
		Long deliveryPersonId = userService.getUserIdFromPrincipal(principal);
		logger.info("Marking order {} as 'Delivered' by delivery person ID: {}", orderId, deliveryPersonId);
		deliveryService.markOrderDelivered(orderId, deliveryPersonId);
		return ResponseEntity.ok("Order marked as Delivered");
	}

	/**
	 * Generate and return a delivery chart for the authenticated delivery person.
	 * 
	 * @param authentication the authentication object containing user details.
	 * @return PNG image as byte array showing delivery performance.
	 */

	@GetMapping("/deliveries/chart")
	public ResponseEntity<byte[]> getDeliveryChart(Authentication authentication) {
		String username = authentication.getName();
		byte[] chart = deliveryService.getDeliveryChart(username);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(chart, headers, HttpStatus.OK);
	}

	/**
	 * Generate and return the total deliveries chart for the authenticated delivery
	 * person.
	 * 
	 * @param authentication the authentication object containing user details.
	 * @return PNG image as byte array showing total deliveries completed by the
	 *         user.
	 */

	@GetMapping("/deliveries/total-chart")
	public ResponseEntity<byte[]> getTotalDeliveriesChart(Authentication authentication) {
		// Get the email from the authenticated user
		String email = authentication.getName();

		// Find the user by email instead of username
		User deliveryUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		// Generate the delivery chart using the user's email
		byte[] chart = deliveryService.getTotalDeliveredChart(deliveryUser.getEmail());

		// Set response headers for an image (PNG)
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);

		// Return the chart in the response
		return new ResponseEntity<>(chart, headers, HttpStatus.OK);
	}

}
