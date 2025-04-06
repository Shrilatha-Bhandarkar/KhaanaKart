package com.onlinefoodorder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
/**
 * Entity representing a customer's review
 */
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    private int rating; // Between 1 and 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

	public Long getReviewId() {
		return reviewId;
	}

	public void setReviewId(Long reviewId) {
		this.reviewId = reviewId;
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

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Review() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Review(Long reviewId, User user, Restaurant restaurant, int rating, String comment,
			LocalDateTime createdAt) {
		super();
		this.reviewId = reviewId;
		this.user = user;
		this.restaurant = restaurant;
		this.rating = rating;
		this.comment = comment;
		this.createdAt = createdAt;
	}

    
}
