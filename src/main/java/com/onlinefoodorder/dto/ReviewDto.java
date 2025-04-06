package com.onlinefoodorder.dto;
import jakarta.validation.constraints.*;
/**
 * DTO representing a restaurant review.
 */
public class ReviewDto {
    @NotNull
    private Long restaurantId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 500)
    private String comment;
    
	public Long getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
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
	public ReviewDto() {
		super();
	}
	public ReviewDto(Long restaurantId, int rating, String comment) {
		super();
		this.restaurantId = restaurantId;
		this.rating = rating;
		this.comment = comment;
	}

    
}
