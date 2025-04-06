package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.Charts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating statistics and chart data for the
 * admin dashboard, including user, order, and restaurant metrics.
 */

@Service
public class AdminDashboardService {

	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final RestaurantRepository restaurantRepository;

	@Autowired
	public AdminDashboardService(UserRepository userRepository, OrderRepository orderRepository,
			OrderItemRepository orderItemRepository, RestaurantRepository restaurantRepository) {
		this.userRepository = userRepository;
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.restaurantRepository = restaurantRepository;
	}

	/**
	 * Retrieve aggregated dashboard statistics for the admin panel, including total
	 * users, orders, revenue, and top performers.
	 *
	 * @return DashboardStatsDto containing various statistical insights.
	 */
	public DashboardStatsDto getDashboardStats() {
		DashboardStatsDto stats = new DashboardStatsDto();

		stats.setTotalUsers(userRepository.count());
		stats.setTotalOrdersToday(getTodayOrderCount());
		stats.setTotalRevenueThisMonth(getMonthlyRevenue());
		stats.setTotalRestaurants(restaurantRepository.count());

		stats.setTopSellingItems(orderItemRepository.findTopSellingItems());
		stats.setMostActiveUsers(orderRepository.findMostActiveUsers());
		stats.setTopRestaurantsByOrders(orderRepository.findTopRestaurantsByOrderCount());
		stats.setTopRestaurantsByRevenue(orderRepository.findTopRestaurantsByRevenue());

		return stats;
	}

	/**
	 * Calculate the total number of orders placed today.
	 *
	 * @return number of today's orders.
	 */
	public long getTodayOrderCount() {
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		return orderRepository.countByCreatedAtAfter(startOfDay); // Changed to createdAt
	}

	/**
	 * Calculate the total revenue generated in the current month.
	 *
	 * @return total revenue as BigDecimal.
	 */
	public BigDecimal getMonthlyRevenue() {
		YearMonth currentYearMonth = YearMonth.now();
		LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
		return orderRepository.findByCreatedAtAfter(startOfMonth).stream() // Changed to createdAt
				.map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Generate a bar chart image of the top 5 selling menu items.
	 *
	 * @return byte array representing the chart image in PNG format.
	 */
	public byte[] getTopSellingItemsChart() {
		var topItems = getDashboardStats().getTopSellingItems().stream().limit(5).toList();
		return Charts.getTopSellingItemsChartBytes(topItems);
	}

	/**
	 * Generate a bar chart image of the top 5 most active users.
	 *
	 * @return byte array representing the chart image in PNG format.
	 */
	public byte[] getActiveUsersChart() {
		var users = getDashboardStats().getMostActiveUsers().stream().limit(5).toList();
		return Charts.getActiveUsersChartBytes(users);
	}

	/**
	 * Generate a bar chart image of the top 5 restaurants by revenue.
	 *
	 * @return byte array representing the chart image in PNG format.
	 */
	public byte[] getRestaurantRevenueChart() {
		var restaurants = getDashboardStats().getTopRestaurantsByRevenue().stream().limit(5).toList();
		return Charts.getRestaurantRevenueChartBytes(restaurants);
	}

	/**
	 * Generate general statistics including user count, today's orders, this
	 * month's revenue, top selling items, and most active users.
	 *
	 * @return a map containing key-value pairs of general metrics.
	 */
	public Map<String, Object> getGeneralStats() {
		DashboardStatsDto stats = getDashboardStats();

		Map<String, Object> generalStats = new HashMap<>();
		generalStats.put("totalUsers", stats.getTotalUsers());
		generalStats.put("totalOrdersToday", stats.getTotalOrdersToday());
		generalStats.put("totalRevenueThisMonth", stats.getTotalRevenueThisMonth());
		generalStats.put("topSellingItems", stats.getTopSellingItems());
		generalStats.put("mostActiveUsers", stats.getMostActiveUsers());

		return generalStats;
	}

	/**
	 * Generate restaurant-related statistics including restaurant count, top
	 * restaurants by number of orders, and top restaurants by revenue.
	 *
	 * @return a map containing key-value pairs of restaurant metrics.
	 */
	public Map<String, Object> getRestaurantStats() {
		DashboardStatsDto stats = getDashboardStats();

		Map<String, Object> restaurantStats = new HashMap<>();
		restaurantStats.put("totalRestaurants", stats.getTotalRestaurants());
		restaurantStats.put("topRestaurantsByOrders",
				stats.getTopRestaurantsByOrders().stream().limit(5).collect(Collectors.toList()));
		restaurantStats.put("topRestaurantsByRevenue",
				stats.getTopRestaurantsByRevenue().stream().limit(5).collect(Collectors.toList()));

		return restaurantStats;
	}
}