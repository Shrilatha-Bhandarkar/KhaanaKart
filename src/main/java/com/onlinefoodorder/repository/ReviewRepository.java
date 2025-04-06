package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Retrieves all reviews for a specific restaurant.
     * 
     * @param restaurantId The restaurant ID.
     * @return List of reviews.
     */
    List<Review> findByRestaurantRestaurantId(Long restaurantId);
}
