package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * Retrieves all menu categories for a specific restaurant.
     * 
     * @param restaurantId The restaurant ID.
     * @return List of menu categories.
     */
    List<MenuCategory> findByRestaurantRestaurantId(Long restaurantId);

    /**
     * Finds a menu category by restaurant ID and category ID.
     * 
     * @param restaurantId The restaurant ID.
     * @param categoryId The category ID.
     * @return Optional containing the menu category if found.
     */
    Optional<MenuCategory> findByRestaurant_RestaurantIdAndCategoryId(Long restaurantId, Long categoryId);
}
