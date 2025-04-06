package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.CouponDto;
import com.onlinefoodorder.service.CouponService;
import com.onlinefoodorder.util.Status.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @Mock
    private CouponService couponService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CouponController couponController;

    private CouponDto couponDto;
    private final String userEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        couponDto = new CouponDto();
        couponDto.setCode("SUMMER20");
        couponDto.setDiscountType(DiscountType.PERCENTAGE);
        couponDto.setDiscountValue(new BigDecimal("20.00"));
        couponDto.setMaxDiscount(new BigDecimal("100.00"));
        couponDto.setMinOrderValue(new BigDecimal("200.00"));
        couponDto.setUsageLimit(100);
        couponDto.setPerUserLimit(1);
        couponDto.setValidFrom(LocalDateTime.now().plusDays(1));
        couponDto.setValidTo(LocalDateTime.now().plusDays(30));
        couponDto.setActive(true);
    }

    private void setupPrincipal() {
        when(principal.getName()).thenReturn(userEmail);
    }

    @Test
    void createGlobalCoupon_Success() {
        setupPrincipal();
        when(couponService.createGlobalCoupon(any(CouponDto.class), eq(userEmail)))
                .thenReturn(couponDto);

        ResponseEntity<CouponDto> response = couponController.createGlobalCoupon(couponDto, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("SUMMER20", response.getBody().getCode());
        verify(couponService).createGlobalCoupon(couponDto, userEmail);
    }

    @Test
    void updateGlobalCoupon_Success() {
        setupPrincipal();
        Long couponId = 1L;
        when(couponService.updateGlobalCoupon(eq(couponId), any(CouponDto.class), eq(userEmail)))
                .thenReturn(couponDto);

        ResponseEntity<CouponDto> response = couponController.updateGlobalCoupon(couponId, couponDto, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("SUMMER20", response.getBody().getCode());
        verify(couponService).updateGlobalCoupon(couponId, couponDto, userEmail);
    }

    @Test
    void deleteGlobalCoupon_Success() {
        setupPrincipal();
        Long couponId = 1L;
        doNothing().when(couponService).deleteGlobalCoupon(couponId, userEmail);

        ResponseEntity<String> response = couponController.deleteGlobalCoupon(couponId, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Global coupon deleted successfully.", response.getBody());
        verify(couponService).deleteGlobalCoupon(couponId, userEmail);
    }

    @Test
    void createRestaurantCoupon_Success() {
        setupPrincipal();
        Long restaurantId = 1L;
        couponDto.setRestaurantId(restaurantId);
        when(couponService.createRestaurantCoupon(any(CouponDto.class), eq(restaurantId), eq(userEmail)))
                .thenReturn(couponDto);

        ResponseEntity<CouponDto> response = 
            couponController.createRestaurantCoupon(restaurantId, couponDto, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(restaurantId, response.getBody().getRestaurantId());
        verify(couponService).createRestaurantCoupon(couponDto, restaurantId, userEmail);
    }

    @Test
    void updateRestaurantCoupon_Success() {
        setupPrincipal();
        Long restaurantId = 1L;
        Long couponId = 1L;
        couponDto.setRestaurantId(restaurantId);
        when(couponService.updateRestaurantCoupon(eq(couponId), any(CouponDto.class), eq(userEmail)))
                .thenReturn(couponDto);

        ResponseEntity<CouponDto> response = 
            couponController.updateRestaurantCoupon(restaurantId, couponId, couponDto, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(restaurantId, response.getBody().getRestaurantId());
        verify(couponService).updateRestaurantCoupon(couponId, couponDto, userEmail);
    }

    @Test
    void deleteRestaurantCoupon_Success() {
        setupPrincipal();
        Long restaurantId = 1L;
        Long couponId = 1L;
        doNothing().when(couponService).deleteRestaurantCoupon(couponId, userEmail);

        ResponseEntity<String> response = 
            couponController.deleteRestaurantCoupon(restaurantId, couponId, principal);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Restaurant coupon deleted successfully.", response.getBody());
        verify(couponService).deleteRestaurantCoupon(couponId, userEmail);
    }

    @Test
    void getCouponByCode_Success() {
        String couponCode = "SUMMER20";
        when(couponService.getCouponByCode(couponCode)).thenReturn(couponDto);

        ResponseEntity<CouponDto> response = couponController.getCouponByCode(couponCode);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(couponCode, response.getBody().getCode());
        verify(couponService).getCouponByCode(couponCode);
    }

    @Test
    void getCouponByCode_NotFound() {
        String couponCode = "INVALIDCODE";
        when(couponService.getCouponByCode(couponCode)).thenReturn(null);

        ResponseEntity<CouponDto> response = couponController.getCouponByCode(couponCode);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}