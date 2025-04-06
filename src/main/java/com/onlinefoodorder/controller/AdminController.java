package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.service.AdminDashboardService;
import com.onlinefoodorder.service.DeliveryService;
import com.onlinefoodorder.util.Status;
import com.onlinefoodorder.exception.UserNotFoundException;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling admin-related operations, such as approving or
 * rejecting users.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AdminDashboardService adminDashboardService;
	@Autowired
	private DeliveryService deliveryService;

	/**
	 * Approves a user based on user ID.
	 * 
	 * @param userId the ID of the user to approve.
	 * @return success message.
	 */
	@PutMapping("/approve/{userId}")
	public ResponseEntity<String> approveUser(@PathVariable Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		user.setApprovalStatus(Status.ApprovalStatus.APPROVED);
		userRepository.save(user);
		logger.info("User with ID {} approved successfully", userId);
		return ResponseEntity.ok("User approved successfully.");
	}

	/**
	 * Rejects a user based on user ID.
	 * 
	 * @param userId the ID of the user to reject.
	 * @return success message.
	 */
	@PutMapping("/reject/{userId}")
	public ResponseEntity<String> rejectUser(@PathVariable Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		user.setApprovalStatus(Status.ApprovalStatus.REJECTED);
		userRepository.save(user);
		logger.info("User with ID {} rejected successfully", userId);
		return ResponseEntity.ok("User rejected successfully.");
	}

	/**
	 * Assign a delivery person to an order.
	 * 
	 * @param orderId          the ID of the order to assign a delivery person to.
	 * @param deliveryPersonId the ID of the delivery person being assigned.
	 * @return success message.
	 */

	@PutMapping("/order/{orderId}/assign-delivery/{deliveryPersonId}")
	public ResponseEntity<String> assignDeliveryPerson(@PathVariable Long orderId,
			@PathVariable Long deliveryPersonId) {

		logger.info("Assigning delivery person {} to order {}", deliveryPersonId, orderId);
		deliveryService.assignDeliveryPerson(orderId, deliveryPersonId);
		return ResponseEntity.ok("Delivery person assigned successfully.");
	}

	/**
	 * Retrieve general statistics for the admin dashboard.
	 * 
	 * @return a map containing general statistics like total users, orders, and
	 *         revenue.
	 */

	@GetMapping("/stats/general")
	public ResponseEntity<Map<String, Object>> getGeneralStats() {
		return ResponseEntity.ok(adminDashboardService.getGeneralStats());
	}

	/**
	 * Retrieve restaurant-specific statistics for the admin dashboard.
	 * 
	 * @return a map containing statistics related to restaurants.
	 */

	@GetMapping("/stats/restaurants")
	public ResponseEntity<Map<String, Object>> getRestaurantStats() {
		return ResponseEntity.ok(adminDashboardService.getRestaurantStats());
	}

	/**
	 * Generate and return a chart image of top-selling items.
	 * 
	 * @return PNG image as byte array representing top-selling food items.
	 */

	// Chart Data Endpoint
	@GetMapping("/stats/charts/top-selling-items")
	public ResponseEntity<byte[]> getTopSellingItemsChartImage() {
		byte[] imageBytes = adminDashboardService.getTopSellingItemsChart();
		return ResponseEntity.ok().header("Content-Type", "image/png")
				.header("Content-Disposition", "inline; filename=\"top-selling-items.png\"").body(imageBytes);
	}

	/**
	 * Generate and return a chart image of active users.
	 * 
	 * @return PNG image as byte array representing active user metrics.
	 */

	@GetMapping("/stats/charts/active-users")
	public ResponseEntity<byte[]> getActiveUsersChartImage() {
		byte[] imageBytes = adminDashboardService.getActiveUsersChart();
		return ResponseEntity.ok().header("Content-Type", "image/png")
				.header("Content-Disposition", "inline; filename=\"active-users.png\"").body(imageBytes);
	}

	/**
	 * Generate and return a chart image of restaurant revenue.
	 * 
	 * @return PNG image as byte array representing revenue per restaurant.
	 */

	@GetMapping("/stats/charts/restaurant-revenue")
	public ResponseEntity<byte[]> getRestaurantRevenueChartImage() {
		byte[] imageBytes = adminDashboardService.getRestaurantRevenueChart();
		return ResponseEntity.ok().header("Content-Type", "image/png")
				.header("Content-Disposition", "inline; filename=\"restaurant-revenue.png\"").body(imageBytes);
	}

	/**
	 * Retrieve complete dashboard statistics for admin overview.
	 * 
	 * @return dashboard statistics DTO containing summarized platform data.
	 */

	@GetMapping("/stats/dashboard")
	public ResponseEntity<DashboardStatsDto> getDashboardStats() {
		return ResponseEntity.ok(adminDashboardService.getDashboardStats());
	}

	/**
	 * Generate and return a chart image representing delivery personnel statistics.
	 * 
	 * @return PNG image as byte array showing delivery performance metrics.
	 */

	@GetMapping("/delivery-stats/chart")
	public ResponseEntity<byte[]> getAllDeliveryStatsChart() {
		byte[] chart = deliveryService.getAllDeliveryPersonsStatsChart();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(chart, headers, HttpStatus.OK);
	}

}