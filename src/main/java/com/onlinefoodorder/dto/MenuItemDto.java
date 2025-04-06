package com.onlinefoodorder.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO representing a menu item in a restaurant.
 */
public class MenuItemDto {
	private long itemId;
	@NotNull
	private long categoryId;

	@NotNull
	private long restaurantId;

	@NotBlank
	@Size(min = 2, max = 50)
	private String name;

	private String description;

	@NotNull
	@Positive
	private BigDecimal price;

	private String imageUrl;

	private boolean vegetarian;

	private boolean available;

	@Min(1)
	private int preparationTimeMin;

	private long quantitySold;

	public MenuItemDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(long restaurantId) {
		this.restaurantId = restaurantId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public boolean isVegetarian() {
		return vegetarian;
	}

	public void setVegetarian(boolean vegetarian) {
		this.vegetarian = vegetarian;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public int getPreparationTimeMin() {
		return preparationTimeMin;
	}

	public void setPreparationTimeMin(int preparationTimeMin) {
		this.preparationTimeMin = preparationTimeMin;
	}

	public MenuItemDto(long itemId, long categoryId, long restaurantId, String name, String description,
			BigDecimal price, String imageUrl, boolean vegetarian, boolean available, int preparationTimeMin) {
		this.itemId = itemId;
		this.categoryId = categoryId;
		this.restaurantId = restaurantId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.vegetarian = vegetarian;
		this.available = available;
		this.preparationTimeMin = preparationTimeMin;
	}

	public MenuItemDto(long itemId, long categoryId, long restaurantId, String name, String description,
			BigDecimal price, String imageUrl, boolean vegetarian, boolean available, int preparationTimeMin,
			long quantitySold) {
		this.itemId = itemId;
		this.categoryId = categoryId;
		this.restaurantId = restaurantId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.vegetarian = vegetarian;
		this.available = available;
		this.preparationTimeMin = preparationTimeMin;
		this.quantitySold = quantitySold;
	}

// Getters and Setters
	public long getQuantitySold() {
		return quantitySold;
	}

	public void setQuantitySold(long quantitySold) {
		this.quantitySold = quantitySold;
	}
}
