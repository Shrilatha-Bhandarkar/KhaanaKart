package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.ReviewDto;
import com.onlinefoodorder.entity.Review;
import com.onlinefoodorder.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing customer reviews of restaurants. Allows users to add,
 * view, and delete reviews. Ensures only the review owner can delete their
 * review.
 */

@RestController
@RequestMapping("/reviews")
public class ReviewController {

	private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
	
	@Autowired
	private ReviewService reviewService;


	/**
	 * Add a review for a restaurant
	 * 
	 * @param reviewDto DTO containing review details
	 * @return Success message
	 */
	@PostMapping("/add")
	public ResponseEntity<String> addReview(@RequestBody ReviewDto reviewDto) {
		logger.info("Received request to add review for restaurant ID: {}", reviewDto.getRestaurantId());
		String response = reviewService.addReview(reviewDto);
		return ResponseEntity.ok(response);
	}

	/**
	 * Fetch all reviews for a specific restaurant
	 * 
	 * @param restaurantId Restaurant ID
	 * @return List of reviews
	 */
	@GetMapping("/restaurant/{restaurantId}")
	public ResponseEntity<List<Review>> getReviews(@PathVariable Long restaurantId) {
		logger.info("Fetching reviews for restaurant ID: {}", restaurantId);
		List<Review> reviews = reviewService.getReviewsByRestaurant(restaurantId);
		return ResponseEntity.ok(reviews);
	}
	/**
	 * Update a review (only allowed by the owner of the review)
	 * 
	 * @param reviewId  Review ID
	 * @param reviewDto Updated review details
	 * @return Success message
	 */
	@PutMapping("/update/{reviewId}")
	public ResponseEntity<String> updateReview(@PathVariable Long reviewId, @RequestBody ReviewDto reviewDto) {
		logger.info("Received request to update review with ID: {}", reviewId);
		String response = reviewService.updateReview(reviewId, reviewDto);
		return ResponseEntity.ok(response);
	}

	/**
	 * Delete a review (only allowed by the owner of the review)
	 * 
	 * @param reviewId Review ID
	 * @return Success message
	 */
	@DeleteMapping("/delete/{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
		logger.info("Received request to delete review with ID: {}", reviewId);
		String response = reviewService.deleteReview(reviewId);
		return ResponseEntity.ok(response);
	}
}
