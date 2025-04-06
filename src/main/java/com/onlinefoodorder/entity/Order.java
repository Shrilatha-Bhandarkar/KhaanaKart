package com.onlinefoodorder.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.onlinefoodorder.util.Status.OrderStatus;

/**
 * Entity representing a customer's order.
 */
@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	@ManyToOne
	@JoinColumn(name = "delivery_address_id", nullable = false)
	private CustomerAddress deliveryAddress;

	@ManyToOne
	@JoinColumn(name = "delivery_person_id")
	private User deliveryPerson;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems;

	@OneToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(nullable = false)
	private BigDecimal totalAmount;

	@Column(nullable = false)
	private BigDecimal deliveryFee = BigDecimal.ZERO;

	@Column(nullable = false)
	private BigDecimal taxAmount = BigDecimal.ZERO;

	@Column(columnDefinition = "TEXT")
	private String specialInstructions;

	@Column
	private LocalDateTime estimatedDeliveryTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status = OrderStatus.PENDING;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@PreUpdate
	public void setUpdatedAt() {
		this.updatedAt = LocalDateTime.now();
	}

	@ManyToOne
	@JoinColumn(name = "coupon_id")
	private Coupon coupon; // Applied coupon

	@Column(nullable = false)
	private BigDecimal discountAmount = BigDecimal.ZERO; // Discounted amount

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public CustomerAddress getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(CustomerAddress deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public User getDeliveryPerson() {
		return deliveryPerson;
	}

	public void setDeliveryPerson(User deliveryPerson) {
		this.deliveryPerson = deliveryPerson;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
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

	public Order() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Coupon getCoupon() {
		return coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Order(Long orderId, User user, Restaurant restaurant, CustomerAddress deliveryAddress, User deliveryPerson,
			List<OrderItem> orderItems, Payment payment, BigDecimal totalAmount, BigDecimal deliveryFee,
			BigDecimal taxAmount, String specialInstructions, LocalDateTime estimatedDeliveryTime, OrderStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt, Coupon coupon, BigDecimal discountAmount) {
		super();
		this.orderId = orderId;
		this.user = user;
		this.restaurant = restaurant;
		this.deliveryAddress = deliveryAddress;
		this.deliveryPerson = deliveryPerson;
		this.orderItems = orderItems;
		this.payment = payment;
		this.totalAmount = totalAmount;
		this.deliveryFee = deliveryFee;
		this.taxAmount = taxAmount;
		this.specialInstructions = specialInstructions;
		this.estimatedDeliveryTime = estimatedDeliveryTime;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.coupon = coupon;
		this.discountAmount = discountAmount;
	}

}
