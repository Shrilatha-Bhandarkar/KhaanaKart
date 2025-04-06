package com.onlinefoodorder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onlinefoodorder.entity.Cart;
import com.onlinefoodorder.entity.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Finds a cart by the associated user.
     * 
     * @param user The user entity.
     * @return Optional containing the cart if found.
     */
    Optional<Cart> findByUser(User user);

    /**
     * Deletes a cart associated with a given user.
     * 
     * @param user The user entity.
     */
    void deleteByUser(User user);
}
