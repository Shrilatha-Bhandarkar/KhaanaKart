package com.onlinefoodorder.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.onlinefoodorder.util.Status;
import com.onlinefoodorder.util.Status.DiscountType;

import jakarta.persistence.*;
/**
 * Entity representing Discount Coupons for orders.
 */
@Entity
@Table(name = "coupons")

public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = true) // Nullable for global coupons
    private Restaurant restaurant;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Column(nullable = true)
    private BigDecimal maxDiscount; // Only for percentage-based discounts

    @Column(nullable = false)
    private BigDecimal minOrderValue;

    @Column(nullable = false)
    private int usageLimit;

    @Column(nullable = false)
    private int perUserLimit;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private Status.DiscountType discountType;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}



	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(BigDecimal discountValue) {
		this.discountValue = discountValue;
	}

	public Status.DiscountType getDiscountType() {
		return discountType;
	}

	public void setDiscountType(Status.DiscountType discountType) {
		this.discountType = discountType;
	}

	public BigDecimal getMaxDiscount() {
		return maxDiscount;
	}

	public void setMaxDiscount(BigDecimal maxDiscount) {
		this.maxDiscount = maxDiscount;
	}

	public BigDecimal getMinOrderValue() {
		return minOrderValue;
	}

	public void setMinOrderValue(BigDecimal minOrderValue) {
		this.minOrderValue = minOrderValue;
	}

	public int getUsageLimit() {
		return usageLimit;
	}

	public void setUsageLimit(int usageLimit) {
		this.usageLimit = usageLimit;
	}

	public int getPerUserLimit() {
		return perUserLimit;
	}

	public void setPerUserLimit(int perUserLimit) {
		this.perUserLimit = perUserLimit;
	}

	public LocalDateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDateTime getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDateTime validTo) {
		this.validTo = validTo;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}



	public Coupon(Long id, String code, Restaurant restaurant, BigDecimal discountValue, BigDecimal maxDiscount,
			BigDecimal minOrderValue, int usageLimit, int perUserLimit, LocalDateTime validFrom, LocalDateTime validTo,
			boolean active, DiscountType discountType) {
		super();
		this.id = id;
		this.code = code;
		this.restaurant = restaurant;
		this.discountValue = discountValue;
		this.maxDiscount = maxDiscount;
		this.minOrderValue = minOrderValue;
		this.usageLimit = usageLimit;
		this.perUserLimit = perUserLimit;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.active = active;
		this.discountType = discountType;
	}

	public Coupon() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}
