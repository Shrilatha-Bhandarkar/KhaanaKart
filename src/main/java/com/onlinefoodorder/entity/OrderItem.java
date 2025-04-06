package com.onlinefoodorder.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

/**
 * Entity representing order items from a customer's order
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderItemId;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne
	@JoinColumn(name = "menu_item_id", nullable = false)
	private MenuItem menuItem;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(columnDefinition = "TEXT")
	private String specialRequest;

	public Long getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getSpecialRequest() {
		return specialRequest;
	}

	public void setSpecialRequest(String specialRequest) {
		this.specialRequest = specialRequest;
	}

	public OrderItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderItem(Long orderItemId, Order order, MenuItem menuItem, int quantity, BigDecimal price,
			String specialRequest) {
		super();
		this.orderItemId = orderItemId;
		this.order = order;
		this.menuItem = menuItem;
		this.quantity = quantity;
		this.price = price;
		this.specialRequest = specialRequest;
	}

}