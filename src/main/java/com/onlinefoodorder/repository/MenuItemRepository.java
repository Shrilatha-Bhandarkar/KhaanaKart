package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.MenuItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Retrieves all menu items belonging to a specific category.
     * 
     * @param categoryId The category ID.
     * @return List of menu items.
     */
    List<MenuItem> findByCategoryCategoryId(long categoryId);

    /**
     * Retrieves all menu items belonging to a specific restaurant.
     * 
     * @param restaurantId The restaurant ID.
     * @return List of menu items.
     */
    List<MenuItem> findByRestaurantRestaurantId(long restaurantId);

    /**
     * Counts the number of menu items in a given category.
     * 
     * @param categoryId The category ID.
     * @return The number of menu items.
     */
    long countByCategoryCategoryId(long categoryId);

    /**
     * Retrieves menu items by their item ID.
     * 
     * @param itemId The item ID.
     * @return List of menu items.
     */
    List<MenuItem> findByItemId(long itemId);
}
