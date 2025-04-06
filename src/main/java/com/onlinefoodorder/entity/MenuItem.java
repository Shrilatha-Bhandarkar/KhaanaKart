package com.onlinefoodorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Entity representing menu item from each category
 */
@Entity
@Table(name = "menu_items")
public class MenuItem {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long itemId;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private MenuCategory category;

	@ManyToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(length = 255)
	private String imageUrl;

	@Column(name = "is_vegetarian",nullable = false)
	private boolean isVegetarian = true;

	@Column(name = "is_available",nullable = false)
	private boolean isAvailable = true;

	@Column(nullable = false)
	private int preparationTimeMin = 15;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public MenuCategory getCategory() {
		return category;
	}

	public void setCategory(MenuCategory category) {
		this.category = category;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
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
		return isVegetarian;
	}

	public void setVegetarian(boolean isVegetarian) {
		this.isVegetarian = isVegetarian;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public int getPreparationTimeMin() {
		return preparationTimeMin;
	}

	public void setPreparationTimeMin(int preparationTimeMin) {
		this.preparationTimeMin = preparationTimeMin;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public MenuItem(long itemId, MenuCategory category, Restaurant restaurant, String name, String description,
			BigDecimal price, String imageUrl, boolean isVegetarian, boolean isAvailable, int preparationTimeMin,
			LocalDateTime createdAt) {
		super();
		this.itemId = itemId;
		this.category = category;
		this.restaurant = restaurant;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.isVegetarian = isVegetarian;
		this.isAvailable = isAvailable;
		this.preparationTimeMin = preparationTimeMin;
		this.createdAt = createdAt;
	}

	public MenuItem() {
		super();
		// TODO Auto-generated constructor stub
	}
}
