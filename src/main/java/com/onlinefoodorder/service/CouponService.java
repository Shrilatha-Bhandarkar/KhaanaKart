package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.CouponDto;
import com.onlinefoodorder.entity.Coupon;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.CouponRepository;
import com.onlinefoodorder.repository.OrderRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.UserRole;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling coupon-related operations, including creation, update,
 * deletion, and retrieval of coupons for both global and restaurant-specific
 * use cases.
 */
@Service
@RequiredArgsConstructor
public class CouponService {
	private static final Logger logger = LoggerFactory.getLogger(CouponService.class);
	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository; // For checking coupon usage

	/**
	 * Admin: Create Global Coupon Creates a global coupon that can be applied
	 * across all restaurants.
	 *
	 * @param dto        Coupon details from the client.
	 * @param adminEmail Admin's email for authentication.
	 * @return The created coupon as DTO.
	 */
	public CouponDto createGlobalCoupon(CouponDto dto, String adminEmail) {
		logger.info("Admin '{}' is attempting to create a global coupon", adminEmail);

		// Check if admin exists
		userRepository.findByEmail(adminEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Prevent duplicate coupon codes
		if (couponRepository.findByCode(dto.getCode()).isPresent()) {
			logger.error("Coupon with code '{}' already exists", dto.getCode());
			throw new IllegalArgumentException("A coupon with this code already exists.");
		}

		// Create and save the global coupon
		Coupon coupon = mapDtoToEntity(dto, null);
		couponRepository.save(coupon);

		logger.info("Global coupon with code '{}' created successfully by '{}'", dto.getCode(), adminEmail);
		return mapEntityToDto(coupon);
	}

	/**
	 * Restaurant Owner: Create Coupon Creates a coupon for a specific restaurant.
	 *
	 * @param dto          Coupon details from the client.
	 * @param restaurantId The ID of the restaurant.
	 * @param ownerEmail   Restaurant owner's email for authentication.
	 * @return The created coupon as DTO.
	 */
	public CouponDto createRestaurantCoupon(CouponDto dto, Long restaurantId, String ownerEmail) {
		logger.info("Restaurant owner '{}' is attempting to create a coupon for restaurant ID '{}'", ownerEmail,
				restaurantId);

		// Check if the restaurant exists
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

		// Check if the owner is authorized to manage the restaurant
		if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
			logger.error("Unauthorized access by '{}'. Restaurant ID '{}' does not belong to them.", ownerEmail,
					restaurantId);
			throw new UnauthorizedAccessException("You can only create coupons for your own restaurant.");
		}

		// Prevent duplicate coupon codes
		if (couponRepository.findByCode(dto.getCode()).isPresent()) {
			logger.error("Coupon with code '{}' already exists", dto.getCode());
			throw new IllegalArgumentException("A coupon with this code already exists.");
		}

		// Prevent multiple active coupons of the same type for a restaurant
		if (dto.isActive() && couponRepository.existsByRestaurantRestaurantIdAndDiscountTypeAndActiveTrue(restaurantId,
				dto.getDiscountType())) {
			logger.error("Multiple active coupons of the same type found for restaurant ID '{}'", restaurantId);
			throw new IllegalArgumentException("Only one active coupon of this type is allowed per restaurant.");
		}

		// Create and save the restaurant coupon
		Coupon coupon = mapDtoToEntity(dto, restaurant);
		couponRepository.save(coupon);

		logger.info("Coupon with code '{}' created successfully for restaurant ID '{}' by owner '{}'", dto.getCode(),
				restaurantId, ownerEmail);
		return mapEntityToDto(coupon);
	}

	/**
	 * Get Coupon by Code (Public) Fetches a coupon by its code, ensuring it is not
	 * expired.
	 *
	 * @param code Coupon code.
	 * @return The coupon as DTO.
	 */
	public CouponDto getCouponByCode(String code) {
		logger.info("Fetching coupon with code '{}'", code);

		// Fetch coupon from the repository
		Coupon coupon = couponRepository.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

		// Prevent applying expired coupons
		if (coupon.getValidTo() != null && coupon.getValidTo().isBefore(LocalDateTime.now())) {
			logger.error("Coupon with code '{}' has expired", code);
			throw new IllegalArgumentException("This coupon has expired.");
		}

		return mapEntityToDto(coupon);
	}

	/**
	 * Admin: Update Global Coupon Allows admins to update global coupons only.
	 *
	 * @param id         Coupon ID.
	 * @param dto        New coupon details from the client.
	 * @param adminEmail Admin's email for authentication.
	 * @return Updated coupon as DTO.
	 */
	@Transactional
	public CouponDto updateGlobalCoupon(Long id, CouponDto dto, String adminEmail) {
		logger.info("Admin '{}' is attempting to update global coupon with ID '{}'", adminEmail, id);

		// Check if admin exists
		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Verify if the user is admin
		if (admin.getRole() != UserRole.ADMIN) {
			logger.error("Unauthorized access by '{}'. Only admin can update global coupons.", adminEmail);
			throw new UnauthorizedAccessException("Only admin can update global coupons.");
		}

		// Fetch coupon and validate it
		Coupon coupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

		// Ensure that the coupon is global and not associated with a restaurant
		if (coupon.getRestaurant() != null) {
			logger.error("Coupon ID '{}' is restaurant-specific, not a global coupon.", id);
			throw new UnauthorizedAccessException("Admins can only update global coupons.");
		}

		// Update coupon fields and save
		updateCouponFields(coupon, dto);
		return mapEntityToDto(coupon);
	}

	/**
	 * Updates a restaurant coupon. Only the respective restaurant owner can perform
	 * this action.
	 *
	 * @param id         The ID of the coupon.
	 * @param dto        The updated coupon details.
	 * @param ownerEmail The email of the restaurant owner.
	 * @return The updated coupon details.
	 */
	@Transactional
	public CouponDto updateRestaurantCoupon(Long id, CouponDto dto, String ownerEmail) {
		Coupon coupon = couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(id, ownerEmail)
				.orElseThrow(
						() -> new UnauthorizedAccessException("You can only update coupons for your own restaurant."));

		updateCouponFields(coupon, dto);
		return mapEntityToDto(coupon);
	}

	/**
	 * Deletes a global coupon. Only an admin can perform this action.
	 *
	 * @param id         The ID of the coupon.
	 * @param adminEmail The email of the admin.
	 */
	@Transactional
	public void deleteGlobalCoupon(Long id, String adminEmail) {
		User admin = userRepository.findByEmail(adminEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (admin.getRole() != UserRole.ADMIN) {
			throw new UnauthorizedAccessException("Only admin can delete global coupons.");
		}

		Coupon coupon = couponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

		if (coupon.getRestaurant() != null) {
			throw new UnauthorizedAccessException("Admins can only delete global coupons.");
		}

		couponRepository.delete(coupon);
	}

	/**
	 * Deletes a restaurant coupon. Only the respective restaurant owner can perform
	 * this action.
	 *
	 * @param id         The ID of the coupon.
	 * @param ownerEmail The email of the restaurant owner.
	 */
	@Transactional
	public void deleteRestaurantCoupon(Long id, String ownerEmail) {
		Coupon coupon = couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(id, ownerEmail)
				.orElseThrow(
						() -> new UnauthorizedAccessException("You can only delete coupons for your own restaurant."));

		couponRepository.delete(coupon);
	}

	/**
	 * Validates whether a user can use a specific coupon based on their past usage.
	 *
	 * @param userId The ID of the user.
	 * @param coupon The coupon to validate.
	 */
	public void validateCouponUsage(Long userId, Coupon coupon) {
		if (coupon == null || coupon.getId() == null) {
			throw new IllegalArgumentException("Invalid coupon.");
		}

		long usageCount = orderRepository.countByUserUserIdAndCouponId(userId, coupon.getId());
		if (usageCount >= coupon.getPerUserLimit()) {
			throw new IllegalArgumentException("You have already used this coupon the maximum allowed times.");
		}
	}

	/**
	 * Helper method: Update Coupon Fields Updates coupon fields from the DTO.
	 *
	 * @param coupon Coupon entity to be updated.
	 * @param dto    Coupon DTO containing new values.
	 */
	private void updateCouponFields(Coupon coupon, CouponDto dto) {
		coupon.setCode(dto.getCode());
		coupon.setDiscountType(dto.getDiscountType());
		coupon.setDiscountValue(dto.getDiscountValue());
		coupon.setMaxDiscount(dto.getMaxDiscount());
		coupon.setMinOrderValue(dto.getMinOrderValue());
		coupon.setUsageLimit(dto.getUsageLimit());
		coupon.setPerUserLimit(dto.getPerUserLimit());
		coupon.setValidFrom(dto.getValidFrom());
		coupon.setValidTo(dto.getValidTo());
		coupon.setActive(dto.isActive());
	}

	/**
	 * Helper method: Convert DTO to Entity Converts the Coupon DTO to a Coupon
	 * entity.
	 *
	 * @param dto        Coupon DTO.
	 * @param restaurant Restaurant associated with the coupon.
	 * @return Coupon entity.
	 */
	private Coupon mapDtoToEntity(CouponDto dto, Restaurant restaurant) {
		return new Coupon(null, dto.getCode(), restaurant, dto.getDiscountValue(), dto.getMaxDiscount(),
				dto.getMinOrderValue(), dto.getUsageLimit(), dto.getPerUserLimit(), dto.getValidFrom(),
				dto.getValidTo(), dto.isActive(), dto.getDiscountType());
	}

	/**
	 * Helper method: Convert Entity to DTO Converts the Coupon entity to a Coupon
	 * DTO.
	 *
	 * @param coupon Coupon entity.
	 * @return Coupon DTO.
	 */
	private CouponDto mapEntityToDto(Coupon coupon) {
		return new CouponDto(coupon.getCode(),
				coupon.getRestaurant() != null ? coupon.getRestaurant().getRestaurantId() : null,
				coupon.getDiscountType(), coupon.getDiscountValue(), coupon.getMaxDiscount(), coupon.getMinOrderValue(),
				coupon.getUsageLimit(), coupon.getPerUserLimit(), coupon.getValidFrom(), coupon.getValidTo(),
				coupon.isActive());
	}
}
