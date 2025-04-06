package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.ReviewDto;
import com.onlinefoodorder.entity.Review;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.ReviewNotFoundException;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.repository.ReviewRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
	private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	/**
	 * Adds a review for a restaurant.
	 * 
	 * @param reviewDto Review details DTO.
	 * @return Success message.
	 */
	public String addReview(ReviewDto reviewDto) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		logger.info("User {} is adding a review for restaurant ID: {}", email, reviewDto.getRestaurantId());

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Restaurant restaurant = restaurantRepository.findById(reviewDto.getRestaurantId())
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

		Review review = new Review();
		review.setUser(user);
		review.setRestaurant(restaurant);
		review.setRating(reviewDto.getRating());
		review.setComment(reviewDto.getComment());
		reviewRepository.save(review);

		updateRestaurantRating(restaurant.getRestaurantId());
		logger.info("Review added successfully for restaurant ID: {}", restaurant.getRestaurantId());
		return "Review added successfully!";
	}

	/**
	 * Fetches all reviews for a given restaurant.
	 * 
	 * @param restaurantId ID of the restaurant.
	 * @return List of reviews.
	 */
	public List<Review> getReviewsByRestaurant(Long restaurantId) {
		logger.info("Fetching reviews for restaurant ID: {}", restaurantId);
		return reviewRepository.findByRestaurantRestaurantId(restaurantId);
	}

	/**
	 * Deletes a review if the authenticated user is the owner.
	 * 
	 * @param reviewId ID of the review to be deleted.
	 * @return Success message.
	 */
	public String deleteReview(Long reviewId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		logger.info("User {} is attempting to delete review ID: {}", email, reviewId);

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ReviewNotFoundException("Review not found"));

		if (!review.getUser().getEmail().equals(email)) {
			logger.warn("User {} attempted to delete a review they do not own", email);
			throw new SecurityException("You can only delete your own reviews.");
		}

		reviewRepository.delete(review);
		updateRestaurantRating(review.getRestaurant().getRestaurantId());
		logger.info("Review ID: {} deleted successfully", reviewId);
		return "Review deleted successfully!";
	}
	public String updateReview(Long reviewId, ReviewDto reviewDto) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		logger.info("User {} is attempting to update review ID: {}", email, reviewId);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ReviewNotFoundException("Review not found"));

		if (!review.getUser().getEmail().equals(email)) {
			logger.warn("User {} attempted to update a review they do not own", email);
			throw new SecurityException("You can only update your own reviews.");
		}

		review.setRating(reviewDto.getRating());
		review.setComment(reviewDto.getComment());
		reviewRepository.save(review);

		updateRestaurantRating(review.getRestaurant().getRestaurantId());
		logger.info("Review ID: {} updated successfully", reviewId);
		return "Review updated successfully!";
	}

	/**
	 * Updates the average rating of a restaurant after a review is added or
	 * deleted.
	 * 
	 * @param restaurantId ID of the restaurant.
	 */
	public void updateRestaurantRating(Long restaurantId) {
		List<Review> reviews = reviewRepository.findByRestaurantRestaurantId(restaurantId);
		double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

		Restaurant restaurant = restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
		restaurant.setRating(avgRating);
		restaurantRepository.save(restaurant);
		logger.info("Updated restaurant ID: {} rating to: {}", restaurantId, avgRating);
	}
}
