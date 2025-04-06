package com.onlinefoodorder.dto;

import java.util.List;

import jakarta.validation.constraints.*;

/**
 * DTO representing a restaurant.
 */
public class RestaurantDto {
	private Long userId;
	
	@NotBlank
	@Size(min = 2, max = 100)
	private String name;

	@NotBlank
	private String address;

	@NotBlank
	@Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
	private String phone;

	@DecimalMin(value = "0.0")
	@DecimalMax(value = "5.0")
	private Double rating;

	private String logoUrl;

	@NotBlank
	private String openingTime;

	@NotBlank
	private String closingTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getOpeningTime() {
		return openingTime;
	}

	public void setOpeningTime(String openingTime) {
		this.openingTime = openingTime;
	}

	public String getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(String closingTime) {
		this.closingTime = closingTime;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}
	public RestaurantDto(@NotBlank @Size(min = 2, max = 100) String name, @NotBlank String address,
			@NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number") String phone,
			@DecimalMin("0.0") @DecimalMax("5.0") Double rating, String logoUrl, @NotBlank String openingTime,
			@NotBlank String closingTime) {
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.rating = rating;
		this.logoUrl = logoUrl;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
	}

	public RestaurantDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	private long restaurantId;

	public long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(long restaurantId) {
		this.restaurantId = restaurantId;
	}


	private List<MenuItemDto> menuItemStats;

	public RestaurantDto(Long restaurantId, String name, List<MenuItemDto> menuItemStats) {
		this.restaurantId = restaurantId;
		this.name = name;
		this.menuItemStats = menuItemStats;
	}

	// Getters and Setters
	public List<MenuItemDto> getMenuItemStats() {
		return menuItemStats;
	}

	public void setMenuItemStats(List<MenuItemDto> menuItemStats) {
		this.menuItemStats = menuItemStats;
	}
}
