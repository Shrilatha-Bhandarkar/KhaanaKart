package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.dto.CouponDto;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.DiscountType;
import com.onlinefoodorder.util.Status.UserRole;
import com.onlinefoodorder.repository.CouponRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CouponIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CouponRepository couponRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private JwtUtil jwtUtil;

	private String adminToken;
	private String ownerToken;
	private Long restaurantId;

	@BeforeEach
	void setup() {
	    // Clear repositories in proper order
	    couponRepository.deleteAll();
	    restaurantRepository.deleteAll();
	    userRepository.deleteAll();
	    
	    // Flush changes to database
	    userRepository.flush();
	    restaurantRepository.flush();
	    couponRepository.flush();
		User admin = new User(null, "admin@example.com", "admin", "pass", "Admin", "One", "1111111111", UserRole.ADMIN,
				true);
		admin.setApprovalStatus(ApprovalStatus.APPROVED);
		userRepository.saveAndFlush(admin);

		User owner = new User(null, "owner@example.com", "owner", "pass", "Owner", "Two", "2222222222", UserRole.RESTAURANT_OWNER,
				true);
		owner.setApprovalStatus(ApprovalStatus.APPROVED);
		userRepository.saveAndFlush(owner);

		Restaurant restaurant = new Restaurant(
			    "Testaurant", "Test Street", "9999999999", 4.5,
			    "logo.png", LocalDateTime.now(),
			    "10:00", "22:00", owner);

		restaurantRepository.save(restaurant);
		restaurantId = restaurant.getRestaurantId();

		adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail());
		ownerToken = "Bearer " + jwtUtil.generateToken(owner.getEmail());
	}

	@Test
	void testCreateGlobalCoupon() throws Exception {
		CouponDto dto = getSampleCouponDto();
		String json = objectMapper.writeValueAsString(dto);

		mockMvc.perform(post("/coupons/global").header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(dto.getCode()));
	}

	@Test
	void testCreateRestaurantCoupon() throws Exception {
		CouponDto dto = getSampleCouponDto();
		String json = objectMapper.writeValueAsString(dto);

		mockMvc.perform(post("/coupons/restaurant/" + restaurantId).header("Authorization", ownerToken)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(dto.getCode()));
	}

	@Test
	void testGetCouponByCode() throws Exception {
		CouponDto dto = getSampleCouponDto();

		// Create global coupon first
		mockMvc.perform(post("/coupons/global").header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/coupons/" + dto.getCode())).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(dto.getCode()));
	}

	@Test
	void testUpdateGlobalCoupon() throws Exception {
		CouponDto dto = getSampleCouponDto();
		String json = objectMapper.writeValueAsString(dto);

		// Create coupon first
		String response = mockMvc
				.perform(post("/coupons/global").header("Authorization", adminToken)
						.contentType(MediaType.APPLICATION_JSON).content(json))
				.andReturn().getResponse().getContentAsString();

		Long couponId = couponRepository.findByCode(dto.getCode()).get().getId();
		dto.setDiscountValue(new BigDecimal("40")); // modify
		json = objectMapper.writeValueAsString(dto);

		mockMvc.perform(put("/coupons/global/" + couponId).header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk())
				.andExpect(jsonPath("$.discountValue").value(40));
	}

	@Test
	void testDeleteGlobalCoupon() throws Exception {
		CouponDto dto = getSampleCouponDto();

		mockMvc.perform(post("/coupons/global").header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk());

		Long couponId = couponRepository.findByCode(dto.getCode()).get().getId();

		mockMvc.perform(delete("/coupons/global/" + couponId).header("Authorization", adminToken))
				.andExpect(status().isOk());
	}

	private CouponDto getSampleCouponDto() {
		return new CouponDto("FOOD50", null, DiscountType.PERCENTAGE, BigDecimal.valueOf(50), BigDecimal.valueOf(100),
				BigDecimal.valueOf(200), 10, 2, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5),
				true);
	}
	@Test
	void testCreateGlobalCouponUnauthorized() throws Exception {
	    CouponDto dto = getSampleCouponDto();
	    String json = objectMapper.writeValueAsString(dto);

	    mockMvc.perform(post("/coupons/global")
	            .header("Authorization", ownerToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isForbidden());
	}
	@Test
	void testCreateGlobalCouponByRestaurantOwner() throws Exception {
	    CouponDto dto = getSampleCouponDto();
	    String json = objectMapper.writeValueAsString(dto);

	    mockMvc.perform(post("/coupons/global")
	            .header("Authorization", ownerToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isForbidden());
	}

	@Test
	void testGetExpiredCoupon() throws Exception {
	    CouponDto dto = getSampleCouponDto();
	    dto.setValidTo(LocalDateTime.now().minusDays(1));
	    String json = objectMapper.writeValueAsString(dto);

	    mockMvc.perform(post("/coupons/global")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isOk());

	    mockMvc.perform(get("/coupons/" + dto.getCode()))
	            .andExpect(status().isBadRequest())
	            .andExpect(content().string("This coupon has expired."));
	}
	@Test
	void testCreateGlobalCouponDuplicateCode() throws Exception {
	    CouponDto dto = getSampleCouponDto();
	    String json = objectMapper.writeValueAsString(dto);

	    // Create first coupon
	    mockMvc.perform(post("/coupons/global")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isOk());

	    // Attempt to create another coupon with the same code
	    mockMvc.perform(post("/coupons/global")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isBadRequest())
	            .andExpect(content().string("A coupon with this code already exists."));	}


}
