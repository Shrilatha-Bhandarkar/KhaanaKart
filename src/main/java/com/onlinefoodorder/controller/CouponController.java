package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.CouponDto;
import com.onlinefoodorder.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling admin-related operations, such as approving or
 * rejecting users. Includes logging, transaction handling, and custom exception
 * handling.
 */
@RestController
@RequestMapping("/coupons")

public class CouponController {

	private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

	@Autowired
	private CouponService couponService;

	/**
	 * Admin: Create Global Coupon
	 * 
	 * @param dto       - Coupon details
	 * @param principal - Logged-in user
	 * @return Created coupon information
	 */
	@PostMapping("/global")
	public ResponseEntity<CouponDto> createGlobalCoupon(@RequestBody CouponDto dto, Principal principal) {
		try {
			logger.info("Creating global coupon. User: {}", principal.getName());
			CouponDto createdCoupon = couponService.createGlobalCoupon(dto, principal.getName());
			logger.info("Global coupon created successfully. Coupon code: {}", dto.getCode());
			return ResponseEntity.ok(createdCoupon);
		} catch (Exception e) {
			logger.error("Error occurred while creating global coupon for user: {}", principal.getName(), e);
			throw e;
		}
	}

	/**
	 * Admin: Update Global Coupon
	 * 
	 * @param id        - Coupon ID
	 * @param dto       - Updated coupon details
	 * @param principal - Logged-in user
	 * @return Updated coupon information
	 */
	@PutMapping("/global/{id}")
	public ResponseEntity<CouponDto> updateGlobalCoupon(@PathVariable Long id, @RequestBody CouponDto dto,
			Principal principal) {
		try {
			logger.info("Updating global coupon with ID: {}. User: {}", id, principal.getName());
			CouponDto updatedCoupon = couponService.updateGlobalCoupon(id, dto, principal.getName());
			logger.info("Global coupon updated successfully. Coupon ID: {}", id);
			return ResponseEntity.ok(updatedCoupon);
		} catch (Exception e) {
			logger.error("Error occurred while updating global coupon with ID: {}", id, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}

	/**
	 * Admin: Delete Global Coupon
	 * 
	 * @param id        - Coupon ID
	 * @param principal - Logged-in user
	 * @return Success message
	 */
	@DeleteMapping("/global/{id}")
	public ResponseEntity<String> deleteGlobalCoupon(@PathVariable Long id, Principal principal) {
		try {
			logger.info("Deleting global coupon with ID: {}. User: {}", id, principal.getName());
			couponService.deleteGlobalCoupon(id, principal.getName());
			logger.info("Global coupon deleted successfully. Coupon ID: {}", id);
			return ResponseEntity.ok("Global coupon deleted successfully.");
		} catch (Exception e) {
			logger.error("Error occurred while deleting global coupon with ID: {}", id, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}

	/**
	 * Restaurant Owner: Create Restaurant Coupon
	 * 
	 * @param restaurantId - Restaurant ID
	 * @param dto          - Coupon details
	 * @param principal    - Logged-in user
	 * @return Created coupon information
	 */
	@PostMapping("/restaurant/{restaurantId}")
	public ResponseEntity<CouponDto> createRestaurantCoupon(@PathVariable Long restaurantId, @RequestBody CouponDto dto,
			Principal principal) {
		try {
			logger.info("Creating restaurant coupon for restaurant ID: {}. User: {}", restaurantId,
					principal.getName());
			CouponDto createdCoupon = couponService.createRestaurantCoupon(dto, restaurantId, principal.getName());
			logger.info("Restaurant coupon created successfully. Coupon code: {}", dto.getCode());
			return ResponseEntity.ok(createdCoupon);
		} catch (Exception e) {
			logger.error("Error occurred while creating restaurant coupon for restaurant ID: {}", restaurantId, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}

	/**
	 * Restaurant Owner: Update Coupon
	 * 
	 * @param restaurantId - Restaurant ID
	 * @param id           - Coupon ID
	 * @param dto          - Updated coupon details
	 * @param principal    - Logged-in user
	 * @return Updated coupon information
	 */
	@PutMapping("/restaurant/{restaurantId}/{id}")
	public ResponseEntity<CouponDto> updateRestaurantCoupon(@PathVariable Long restaurantId, @PathVariable Long id,
			@RequestBody CouponDto dto, Principal principal) {
		try {
			logger.info("Updating restaurant coupon with ID: {} for restaurant ID: {}. User: {}", id, restaurantId,
					principal.getName());
			CouponDto updatedCoupon = couponService.updateRestaurantCoupon(id, dto, principal.getName());
			logger.info("Restaurant coupon updated successfully. Coupon ID: {}", id);
			return ResponseEntity.ok(updatedCoupon);
		} catch (Exception e) {
			logger.error("Error occurred while updating restaurant coupon with ID: {} for restaurant ID: {}", id,
					restaurantId, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}

	/**
	 * Restaurant Owner: Delete Coupon
	 * 
	 * @param restaurantId - Restaurant ID
	 * @param id           - Coupon ID
	 * @param principal    - Logged-in user
	 * @return Success message
	 */
	@DeleteMapping("/restaurant/{restaurantId}/{id}")
	public ResponseEntity<String> deleteRestaurantCoupon(@PathVariable Long restaurantId, @PathVariable Long id,
			Principal principal) {
		try {
			logger.info("Deleting restaurant coupon with ID: {} for restaurant ID: {}. User: {}", id, restaurantId,
					principal.getName());
			couponService.deleteRestaurantCoupon(id, principal.getName());
			logger.info("Restaurant coupon deleted successfully. Coupon ID: {}", id);
			return ResponseEntity.ok("Restaurant coupon deleted successfully.");
		} catch (Exception e) {
			logger.error("Error occurred while deleting restaurant coupon with ID: {} for restaurant ID: {}", id,
					restaurantId, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}

	/**
	 * Get Coupon by Code (Anyone can access)
	 * 
	 * @param code - Coupon code
	 * @return Coupon details
	 */
	@GetMapping("/{code}")
	public ResponseEntity<CouponDto> getCouponByCode(@PathVariable String code) {
		try {
			logger.info("Fetching coupon with code: {}", code);
			CouponDto coupon = couponService.getCouponByCode(code);
			logger.info("Coupon fetched successfully. Coupon code: {}", code);
			return ResponseEntity.ok(coupon);
		} catch (Exception e) {
			logger.error("Error occurred while fetching coupon with code: {}", code, e);
			throw e; // Will be handled by GlobalExceptionHandler
		}
	}
}
