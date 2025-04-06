package com.onlinefoodorder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Retrieves all restaurants owned by a specific user.
     * 
     * @param owner The owner entity.
     * @return List of restaurants owned by the user.
     */
    List<Restaurant> findByOwner(User owner);
}
