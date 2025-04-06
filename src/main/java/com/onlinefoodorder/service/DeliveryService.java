package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.OrderDto;

import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.DeliveryException;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.repository.OrderItemRepository;
import com.onlinefoodorder.repository.OrderRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Charts;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.util.Status.UserRole;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

	private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Fetch all assigned orders for a delivery person.
	 *
	 * @param deliveryPersonId The ID of the delivery person.
	 * @return A list of assigned orders.
	 */
	public List<OrderDto> getAssignedOrders(Long deliveryPersonId) {
		if (deliveryPersonId == null) {
			throw new IllegalArgumentException("Delivery person ID cannot be null");
		}

		logger.info("Retrieving assigned orders for delivery person ID: {}", deliveryPersonId);
		List<Order> orders = orderRepository.findByDeliveryPerson_UserIdAndStatus(deliveryPersonId,
				OrderStatus.ASSIGNED);
		return orders.stream().map(this::mapToDto).collect(Collectors.toList());
	}

	/**
	 * Mark an order as "Out for Delivery".
	 *
	 * @param orderId          The order ID.
	 * @param deliveryPersonId The ID of the delivery person.
	 */
	@Transactional
	public void markOrderOutForDelivery(Long orderId, Long deliveryPersonId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (!order.getDeliveryPerson().getUserId().equals(deliveryPersonId)) {
			throw new DeliveryException("Unauthorized: You are not assigned to this order.");
		}

		if (order.getStatus() != OrderStatus.ASSIGNED) {
			throw new DeliveryException("Order is not in an assignable state.");
		}

		order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
		orderRepository.save(order);
		logger.info("Order {} marked as 'Out for Delivery' by delivery person ID: {}", orderId, deliveryPersonId);
	}

	/**
	 * Mark an order as "Delivered".
	 *
	 * @param orderId          The order ID.
	 * @param deliveryPersonId The ID of the delivery person.
	 */
	@Transactional
	public void markOrderDelivered(Long orderId, Long deliveryPersonId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		if (!order.getDeliveryPerson().getUserId().equals(deliveryPersonId)) {
			throw new IllegalArgumentException("Unauthorized: You are not assigned to this order.");
		}

		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
			throw new IllegalStateException("Order is not out for delivery.");
		}

		order.setStatus(OrderStatus.DELIVERED);
		orderRepository.save(order);
		logger.info("Order {} marked as 'Delivered' by delivery person ID: {}", orderId, deliveryPersonId);
	}

	/**
	 * Assign a delivery person to an order.
	 *
	 * @param orderId          The order ID.
	 * @param deliveryPersonId The ID of the delivery person.
	 */
	@Transactional
	public void assignDeliveryPerson(Long orderId, Long deliveryPersonId) {
		logger.info("Assigning order {} to delivery person {}", orderId, deliveryPersonId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID " + orderId));

		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARING) {
			throw new IllegalStateException(
					"Order " + orderId + " is not assignable. Current status: " + order.getStatus());
		}

		logger.debug("Fetching delivery person ID: {}", deliveryPersonId);
		User deliveryPerson = userRepository.findById(deliveryPersonId).orElseThrow(
				() -> new ResourceNotFoundException("Delivery person with ID " + deliveryPersonId + " not found"));

		if (!UserRole.DELIVERY_PERSON.equals(deliveryPerson.getRole())) {
			throw new DeliveryException("User ID " + deliveryPersonId + " is not a valid delivery person.");
		}

		order.setDeliveryPerson(deliveryPerson);
		order.setStatus(OrderStatus.ASSIGNED);
		orderRepository.save(order);

		logger.info("Order {} successfully assigned to delivery person {}", orderId, deliveryPersonId);
	}

	/**
	 * Convert Order entity to DTO.
	 *
	 * @param order The Order entity.
	 * @return The Order DTO.
	 */
	private OrderDto mapToDto(Order order) {
		OrderDto orderDto = new OrderDto();

		orderDto.setOrderId(order.getOrderId());
		orderDto.setUserId(order.getUser().getUserId() != null ? order.getUser().getUserId() : null);
		orderDto.setRestaurantId(order.getRestaurant() != null ? order.getRestaurant().getRestaurantId() : null);
		orderDto.setDeliveryAddressId(
				order.getDeliveryAddress() != null ? order.getDeliveryAddress().getAddressId() : null);
		orderDto.setDeliveryPersonId(order.getDeliveryPerson() != null ? order.getDeliveryPerson().getUserId() : null);
		orderDto.setTotalAmount(order.getTotalAmount());
		orderDto.setDeliveryFee(order.getDeliveryFee());
		orderDto.setTaxAmount(order.getTaxAmount());
		orderDto.setSpecialInstructions(order.getSpecialInstructions());
		orderDto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
		orderDto.setStatus(order.getStatus());
		orderDto.setCreatedAt(order.getCreatedAt());
		orderDto.setUpdatedAt(order.getUpdatedAt());
		orderDto.setPayment(order.getPayment());

		return orderDto;
	}

	public byte[] getDeliveryChart(String email) {
		if (email == null) {
			throw new IllegalArgumentException("Email cannot be null");
		}

		List<Object[]> result = orderRepository.getDeliveredOrdersPerDay(email);

		List<Date> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();

		// Handle empty data case
		if (result.isEmpty()) {
			// Return a chart with default/empty data
			labels.add(new Date(System.currentTimeMillis()));
			values.add(0L);
		} else {
			for (Object[] row : result) {
				labels.add((Date) row[0]);
				values.add((Long) row[1]);
			}
		}

		return Charts.generateDeliveryLineChart("Deliveries Over Time", "Date", "Orders Delivered", labels, values);
	}

	public byte[] getTotalDeliveredChart(String email) {
		if (email == null) {
			throw new IllegalArgumentException("Email cannot be null");
		}

		OrderStatus status = OrderStatus.DELIVERED;
		List<Object[]> result = orderRepository.getTotalOrdersDeliveredByEmail(email, status);

		if (result.isEmpty()) {
			return Charts.generateTotalDeliveredChart("Orders Delivered", "Delivery Person", "Orders", List.of("You"),
					List.of(0L));
		}

		String name = (String) result.get(0)[0];
		Long count = (Long) result.get(0)[1];

		return Charts.generateTotalDeliveredChart("Total Orders Delivered", "Delivery Person", "Orders", List.of(name),
				List.of(count));
	}

	public byte[] getAllDeliveryPersonsStatsChart() {
		List<Object[]> results = orderRepository.getAllDeliveryPersonsStats();

		List<String> names = new ArrayList<>();
		List<Long> counts = new ArrayList<>();

		// Handle empty data case
		if (results.isEmpty()) {
			names.add("No deliveries");
			counts.add(0L);
		} else {
			for (Object[] row : results) {
				names.add((String) row[0]);
				counts.add((Long) row[1]);
			}
		}

		return Charts.getDeliveryPersonDailyChart("Orders Delivered by Each Delivery Person", "Delivery Person",
				"Total Orders", names, counts);
	}

}
