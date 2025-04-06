package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.Coupon;
import com.onlinefoodorder.util.Status.DiscountType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Finds a coupon by its unique code.
     * 
     * @param code The coupon code.
     * @return Optional containing the coupon if found.
     */
    Optional<Coupon> findByCode(String code);

    /**
     * Checks if an active discount exists for a specific restaurant.
     * 
     * @param restaurantId The restaurant ID.
     * @param discountType The discount type.
     * @return True if an active discount exists, otherwise false.
     */
    boolean existsByRestaurantRestaurantIdAndDiscountTypeAndActiveTrue(Long restaurantId, DiscountType discountType);

    /**
     * Finds a coupon by ID, owner email, and ensures it belongs to a restaurant.
     * 
     * @param id The coupon ID.
     * @param ownerEmail The owner's email.
     * @return Optional containing the coupon if found.
     */
    Optional<Coupon> findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(Long id, String ownerEmail);
    /**
     * Retrieve all coupons associated with a specific restaurant.
     * 
     * @param restaurantId the ID of the restaurant whose coupons are to be fetched.
     * @return list of coupons belonging to the given restaurant.
     */

	List<Coupon> findByRestaurantRestaurantId(long restaurantId);
}

