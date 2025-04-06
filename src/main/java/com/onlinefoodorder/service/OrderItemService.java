package com.onlinefoodorder.service;

import com.onlinefoodorder.entity.OrderItem;
import com.onlinefoodorder.repository.OrderItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing order items, including bulk saving.
 */
@Service
public class OrderItemService {

	private static final Logger logger = LoggerFactory.getLogger(OrderItemService.class);
	private final OrderItemRepository orderItemRepository;

	/**
	 * Constructor injection for dependencies.
	 *
	 * @param orderItemRepository Repository for order items.
	 */
	public OrderItemService(OrderItemRepository orderItemRepository) {
		this.orderItemRepository = orderItemRepository;
	}

	/**
	 * Saves a list of order items in bulk.
	 *
	 * @param orderItems List of order items to be saved.
	 */
	@Transactional
	public void saveAll(List<OrderItem> orderItems) {
		logger.info("Saving {} order items.", orderItems.size());
		orderItemRepository.saveAll(orderItems);
		logger.info("Successfully saved all order items.");
	}
}
