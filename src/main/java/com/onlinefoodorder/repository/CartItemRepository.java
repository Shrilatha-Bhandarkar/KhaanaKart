package com.onlinefoodorder.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onlinefoodorder.entity.CartItem;
import com.onlinefoodorder.entity.Cart;
import com.onlinefoodorder.entity.MenuItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Retrieves all cart items associated with the given cart.
     * 
     * @param cart The cart entity.
     * @return List of cart items.
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Retrieves a cart item by cart and menu item.
     * Ensures a user cannot have duplicate items in the cart.
     * 
     * @param cart The cart entity.
     * @param menuItem The menu item entity.
     * @return Optional containing the cart item if found.
     */
    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);

    /**
     * Deletes all cart items associated with the given cart.
     * Used when a user clears the cart or completes checkout.
     * 
     * @param cart The cart entity.
     */
    void deleteByCart(Cart cart);
}
