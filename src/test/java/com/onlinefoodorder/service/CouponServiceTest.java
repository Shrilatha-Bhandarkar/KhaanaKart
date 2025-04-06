package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.CouponDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.exception.*;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.Status.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private CouponService couponService;
    
    private User admin;
    private User owner;
    private Restaurant restaurant;
    private Coupon globalCoupon;
    private Coupon restaurantCoupon;
    private CouponDto couponDto;
    
    @BeforeEach
    void setUp() {
        // Admin user
        admin = new User();
        admin.setUserId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);
        
        // Restaurant owner
        owner = new User();
        owner.setUserId(2L);
        owner.setEmail("owner@example.com");
        owner.setRole(UserRole.RESTAURANT_OWNER);
        
        // Restaurant
        restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setOwner(owner);
        
        // Global coupon
        globalCoupon = new Coupon();
        globalCoupon.setId(1L);
        globalCoupon.setCode("GLOBAL10");
        globalCoupon.setRestaurant(null);
        globalCoupon.setDiscountType(DiscountType.PERCENTAGE);
        globalCoupon.setDiscountValue(new BigDecimal("10"));
        globalCoupon.setActive(true);
        
        // Restaurant coupon
        restaurantCoupon = new Coupon();
        restaurantCoupon.setId(2L);
        restaurantCoupon.setCode("RESTAURANT20");
        restaurantCoupon.setRestaurant(restaurant);
        restaurantCoupon.setDiscountType(DiscountType.FIXED);
        restaurantCoupon.setDiscountValue(new BigDecimal("5"));
        restaurantCoupon.setActive(true);
        
        // Coupon DTO
        couponDto = new CouponDto();
        couponDto.setCode("TEST15");
        couponDto.setDiscountType(DiscountType.PERCENTAGE);
        couponDto.setDiscountValue(new BigDecimal("15"));
        couponDto.setMaxDiscount(new BigDecimal("10"));
        couponDto.setMinOrderValue(new BigDecimal("50"));
        couponDto.setValidFrom(LocalDateTime.now().minusDays(1));
        couponDto.setValidTo(LocalDateTime.now().plusDays(30));
        couponDto.setActive(true);
    }
    
    @Test
    void createGlobalCoupon_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(globalCoupon);
        
        CouponDto result = couponService.createGlobalCoupon(couponDto, "admin@example.com");
        
        assertNotNull(result);
        assertEquals("GLOBAL10", result.getCode());
        verify(couponRepository).save(any(Coupon.class));
    }
    
    @Test
    void createGlobalCoupon_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class,
            () -> couponService.createGlobalCoupon(couponDto, "admin@example.com"));
    }
    
    @Test
    void createGlobalCoupon_DuplicateCode_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(globalCoupon));
        
        assertThrows(IllegalArgumentException.class,
            () -> couponService.createGlobalCoupon(couponDto, "admin@example.com"));
    }
    
    @Test
    void createRestaurantCoupon_Success() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(couponRepository.existsByRestaurantRestaurantIdAndDiscountTypeAndActiveTrue(anyLong(), any()))
            .thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(restaurantCoupon);
        
        CouponDto result = couponService.createRestaurantCoupon(couponDto, 1L, "owner@example.com");
        
        assertNotNull(result);
        assertEquals("RESTAURANT20", result.getCode());
        assertEquals(1L, result.getRestaurantId());
        verify(couponRepository).save(any(Coupon.class));
    }
    
    @Test
    void createRestaurantCoupon_UnauthorizedOwner_ThrowsException() {
        User otherOwner = new User();
        otherOwner.setUserId(3L);
        otherOwner.setEmail("other@example.com");
        restaurant.setOwner(otherOwner);
        
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.createRestaurantCoupon(couponDto, 1L, "owner@example.com"));
    }
    
    @Test
    void createRestaurantCoupon_DuplicateType_ThrowsException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(couponRepository.existsByRestaurantRestaurantIdAndDiscountTypeAndActiveTrue(anyLong(), any()))
            .thenReturn(true);
        
        assertThrows(IllegalArgumentException.class,
            () -> couponService.createRestaurantCoupon(couponDto, 1L, "owner@example.com"));
    }
    
    @Test
    void getCouponByCode_Success() {
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(globalCoupon));
        
        CouponDto result = couponService.getCouponByCode("GLOBAL10");
        
        assertNotNull(result);
        assertEquals("GLOBAL10", result.getCode());
    }
    
    @Test
    void getCouponByCode_NotFound_ThrowsException() {
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class,
            () -> couponService.getCouponByCode("INVALID"));
    }
    
    @Test
    void getCouponByCode_Expired_ThrowsException() {
        globalCoupon.setValidTo(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(globalCoupon));
        
        assertThrows(IllegalArgumentException.class,
            () -> couponService.getCouponByCode("EXPIRED"));
    }
    
    @Test
    void updateGlobalCoupon_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(globalCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(globalCoupon);
        
        CouponDto result = couponService.updateGlobalCoupon(1L, couponDto, "admin@example.com");
        
        assertNotNull(result);
        verify(couponRepository).save(globalCoupon);
    }
    
    @Test
    void updateGlobalCoupon_NonAdmin_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(owner));
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.updateGlobalCoupon(1L, couponDto, "owner@example.com"));
    }
    
    @Test
    void updateGlobalCoupon_RestaurantCoupon_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(restaurantCoupon));
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.updateGlobalCoupon(2L, couponDto, "admin@example.com"));
    }
    
    @Test
    void updateRestaurantCoupon_Success() {
        when(couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(anyLong(), anyString()))
            .thenReturn(Optional.of(restaurantCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(restaurantCoupon);
        
        CouponDto result = couponService.updateRestaurantCoupon(2L, couponDto, "owner@example.com");
        
        assertNotNull(result);
        verify(couponRepository).save(restaurantCoupon);
    }
    
    @Test
    void updateRestaurantCoupon_Unauthorized_ThrowsException() {
        when(couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(anyLong(), anyString()))
            .thenReturn(Optional.empty());
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.updateRestaurantCoupon(2L, couponDto, "other@example.com"));
    }
    
    @Test
    void deleteGlobalCoupon_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(globalCoupon));
        
        couponService.deleteGlobalCoupon(1L, "admin@example.com");
        
        verify(couponRepository).delete(globalCoupon);
    }
    
    @Test
    void deleteGlobalCoupon_RestaurantCoupon_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(restaurantCoupon));
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.deleteGlobalCoupon(2L, "admin@example.com"));
    }
    
    @Test
    void deleteRestaurantCoupon_Success() {
        when(couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(anyLong(), anyString()))
            .thenReturn(Optional.of(restaurantCoupon));
        
        couponService.deleteRestaurantCoupon(2L, "owner@example.com");
        
        verify(couponRepository).delete(restaurantCoupon);
    }
    
    @Test
    void deleteRestaurantCoupon_Unauthorized_ThrowsException() {
        when(couponRepository.findByIdAndRestaurantOwnerEmailAndRestaurantIsNotNull(anyLong(), anyString()))
            .thenReturn(Optional.empty());
        
        assertThrows(UnauthorizedAccessException.class,
            () -> couponService.deleteRestaurantCoupon(2L, "other@example.com"));
    }
    
    @Test
    void validateCouponUsage_UnderLimit_Success() {
        when(orderRepository.countByUserUserIdAndCouponId(anyLong(), anyLong()))
            .thenReturn(1L);
        
        restaurantCoupon.setPerUserLimit(2);
        couponService.validateCouponUsage(1L, restaurantCoupon);
    }
    
    @Test
    void validateCouponUsage_OverLimit_ThrowsException() {
        when(orderRepository.countByUserUserIdAndCouponId(anyLong(), anyLong()))
            .thenReturn(2L);
        
        restaurantCoupon.setPerUserLimit(1);
        assertThrows(IllegalArgumentException.class,
            () -> couponService.validateCouponUsage(1L, restaurantCoupon));
    }
}