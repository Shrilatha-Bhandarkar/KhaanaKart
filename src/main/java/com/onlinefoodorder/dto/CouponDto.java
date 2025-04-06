package com.onlinefoodorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.onlinefoodorder.util.Status.DiscountType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for managing coupon details.
 */
public class CouponDto {
	private Long id;
	@NotBlank
    private String code;
	
	private Long restaurantId; 
	
	@NotBlank
    private DiscountType discountType;

    @NotNull
    @Positive
    private BigDecimal discountValue;

    @NotNull
    @Positive
    private BigDecimal maxDiscount;

    @NotNull
    @Positive
    private BigDecimal minOrderValue;

    @Min(1)
    private int usageLimit;

    @Min(1)
    private int perUserLimit;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validTo;

    private boolean active;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}



	public DiscountType getDiscountType() {
		return discountType;
	}

	public void setDiscountType(DiscountType discountType) {
		this.discountType = discountType;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(BigDecimal discountValue) {
		this.discountValue = discountValue;
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

	public CouponDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CouponDto(long id,@NotBlank String code, Long restaurantId, @NotBlank DiscountType discountType,
			@NotNull @Positive BigDecimal discountValue, @NotNull @Positive BigDecimal maxDiscount,
			@NotNull @Positive BigDecimal minOrderValue, @Min(1) int usageLimit, @Min(1) int perUserLimit,
			@NotNull LocalDateTime validFrom, @NotNull LocalDateTime validTo, boolean active) {
		super();
		this.id=id;
		this.code = code;
		this.restaurantId = restaurantId;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.maxDiscount = maxDiscount;
		this.minOrderValue = minOrderValue;
		this.usageLimit = usageLimit;
		this.perUserLimit = perUserLimit;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.active = active;
	}

	public CouponDto(@NotBlank String code, Long restaurantId, @NotBlank DiscountType discountType,
			@NotNull @Positive BigDecimal discountValue, @NotNull @Positive BigDecimal maxDiscount,
			@NotNull @Positive BigDecimal minOrderValue, @Min(1) int usageLimit, @Min(1) int perUserLimit,
			@NotNull LocalDateTime validFrom, @NotNull LocalDateTime validTo, boolean active) {
		super();
		this.code = code;
		this.restaurantId = restaurantId;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.maxDiscount = maxDiscount;
		this.minOrderValue = minOrderValue;
		this.usageLimit = usageLimit;
		this.perUserLimit = perUserLimit;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.active = active;
	}

	public Long getId() {
		
		return id;
	}


}
