package com.onlinefoodorder.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * Entity representing a restaurant
 */

@Entity
@Table(name = "restaurants")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long restaurantId;

	public Restaurant() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false, length = 15)
	private String phone;

	private double rating = 0.0;

	private String logoUrl;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private String openingTime;

	@Column(nullable = false)
	private String closingTime;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;

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

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Restaurant(long restaurantId, String name, String address, String phone, double rating, String logoUrl,
			LocalDateTime createdAt, String openingTime, String closingTime, User owner) {
		super();
		this.restaurantId = restaurantId;
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.rating = rating;
		this.logoUrl = logoUrl;
		this.createdAt = createdAt;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.owner = owner;
	}

	public Restaurant(String name, String address, String phone, double rating, String logoUrl, LocalDateTime createdAt,
			String openingTime, String closingTime, User owner) {
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.rating = rating;
		this.logoUrl = logoUrl;
		this.createdAt = createdAt;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.owner = owner;
	}
}
