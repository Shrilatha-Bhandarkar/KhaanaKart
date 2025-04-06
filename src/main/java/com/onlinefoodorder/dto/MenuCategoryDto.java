package com.onlinefoodorder.dto;

import jakarta.validation.constraints.*;

/**
 * DTO representing a menu category in a restaurant.
 */
public class MenuCategoryDto {
	private long categoryId;

	@NotNull
	private long restaurantId;

	@Min(0)
	private long itemCount;

	@NotBlank
	@Size(min = 2, max = 50)
	private String name;

	private String description;

	public long getItemCount() {
		return itemCount;
	}

	public void setItemCount(long itemCount) {
		this.itemCount = itemCount;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public MenuCategoryDto() {
		super();
		// TODO Auto-generated constructor stub
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

	public MenuCategoryDto(long categoryId, long restaurantId, String name, String description, long itemCount) {
		this.categoryId = categoryId;
		this.restaurantId = restaurantId;
		this.name = name;
		this.description = description;
		this.itemCount = itemCount;
	}

}
