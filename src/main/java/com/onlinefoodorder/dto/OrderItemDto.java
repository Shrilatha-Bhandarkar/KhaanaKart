package com.onlinefoodorder.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO representing an individual item in an order.
 */
public class OrderItemDto {
	private Long orderItemId;

	@NotNull
	private Long orderId;

	@NotNull
	private Long menuItemId;

	@Min(1)
	private int quantity;

	@NotNull
	@Positive
	private BigDecimal price;

	private String specialRequest;

	public Long getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getMenuItemId() {
		return menuItemId;
	}

	public void setMenuItemId(Long menuItemId) {
		this.menuItemId = menuItemId;
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

	public OrderItemDto(Long orderItemId, Long orderId, Long menuItemId, int quantity, BigDecimal price,
			String specialRequest) {
		super();
		this.orderItemId = orderItemId;
		this.orderId = orderId;
		this.menuItemId = menuItemId;
		this.quantity = quantity;
		this.price = price;
		this.specialRequest = specialRequest;
	}

	public OrderItemDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
