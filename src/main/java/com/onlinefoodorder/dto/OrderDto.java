package com.onlinefoodorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.onlinefoodorder.entity.Payment;
import com.onlinefoodorder.util.Status.OrderStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO representing an order placed by a customer.
 */
public class OrderDto {
	private Long orderId;
	private Long userId;
	private Long restaurantId;
	private Long deliveryAddressId;
	private Long deliveryPersonId;
	@NotEmpty
	private List<OrderItemDto> orderItems;

	@NotNull
	@Positive
	private BigDecimal totalAmount;
	@NotNull
	@Positive
	private BigDecimal deliveryFee;

	@NotNull
	@Positive
	private BigDecimal taxAmount;

	private String specialInstructions;

	@NotNull
	private LocalDateTime estimatedDeliveryTime;

	@NotNull
	private OrderStatus status;

	@NotNull
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	private Payment payment;
	
	private String couponCode; 

	private BigDecimal discountAmount; 

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}

	public Long getDeliveryAddressId() {
		return deliveryAddressId;
	}

	public void setDeliveryAddressId(Long deliveryAddressId) {
		this.deliveryAddressId = deliveryAddressId;
	}

	public Long getDeliveryPersonId() {
		return deliveryPersonId;
	}

	public void setDeliveryPersonId(Long deliveryPersonId) {
		this.deliveryPersonId = deliveryPersonId;
	}

	public List<OrderItemDto> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItemDto> orderItems) {
		this.orderItems = orderItems;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(BigDecimal deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public String getSpecialInstructions() {
		return specialInstructions;
	}

	public void setSpecialInstructions(String specialInstructions) {
		this.specialInstructions = specialInstructions;
	}

	public LocalDateTime getEstimatedDeliveryTime() {
		return estimatedDeliveryTime;
	}

	public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
		this.estimatedDeliveryTime = estimatedDeliveryTime;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public OrderDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public OrderDto(Long orderId, Long userId, Long restaurantId, Long deliveryAddressId, Long deliveryPersonId,
			List<OrderItemDto> orderItems, BigDecimal totalAmount, BigDecimal deliveryFee, BigDecimal taxAmount,
			String specialInstructions, LocalDateTime estimatedDeliveryTime, OrderStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt, Payment payment, String couponCode,
			BigDecimal discountAmount) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.restaurantId = restaurantId;
		this.deliveryAddressId = deliveryAddressId;
		this.deliveryPersonId = deliveryPersonId;
		this.orderItems = orderItems;
		this.totalAmount = totalAmount;
		this.deliveryFee = deliveryFee;
		this.taxAmount = taxAmount;
		this.specialInstructions = specialInstructions;
		this.estimatedDeliveryTime = estimatedDeliveryTime;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.payment = payment;
		this.couponCode = couponCode;
		this.discountAmount = discountAmount;
	}

}