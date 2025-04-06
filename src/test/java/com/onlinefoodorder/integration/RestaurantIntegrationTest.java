package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.dto.RestaurantDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RestaurantIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private RestaurantRepository restaurantRepository;

	private RestaurantDto sampleRestaurantDto;
	private User mockUser;

	@BeforeEach
	public void setup() {
		restaurantRepository.deleteAll();
		userRepository.deleteAll();
		
		mockUser = new User();
		mockUser.setEmail("owner@example.com");
		mockUser.setUsername("owner"); // Required if non-null
		mockUser.setPassword("password");
		mockUser.setFirstName("John");
		mockUser.setLastName("Doe");
		mockUser.setPhone("9876543210");
		mockUser.setRole(UserRole.RESTAURANT_OWNER);
		mockUser.setApprovalStatus(ApprovalStatus.APPROVED);
		mockUser.setActive(true);

		mockUser = userRepository.save(mockUser);
		
	    sampleRestaurantDto = new RestaurantDto(
	            "Pizza Palace",
	            "123 Main St",
	            "9876543210",
	            4.5,
	            "https://logo.com/logo.png",
	            "09:00:00",
	            "22:00:00"
	        );


	}

	@Test
	public void testAddRestaurant_WithJWT_Success() throws Exception {
	    mockUser.setApprovalStatus(ApprovalStatus.APPROVED);
	    userRepository.saveAndFlush(mockUser);

	    String token = jwtUtil.generateToken(mockUser.getEmail());

	    mockMvc.perform(post("/restaurant/add")
	            .header("Authorization", "Bearer " + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(sampleRestaurantDto))
	            .with(csrf()))
	            .andExpect(status().isOk());
	}



	@Test
	@WithMockUser(username = "owner@example.com", roles = { "CUSTOMER" }) // Wrong role
	public void testAddRestaurant_ForbiddenForWrongRole() throws Exception {
	    mockUser.setRole(UserRole.CUSTOMER);
	    userRepository.save(mockUser);  // Save the updated role in DB

	    mockMvc.perform(post("/restaurant/add")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(sampleRestaurantDto)))
	            .andExpect(status().isForbidden())
	            .andExpect(content().string("Only restaurant owners can add restaurants."));
	}


	@Test
	@WithMockUser(username = "owner@example.com", roles = { "RESTAURANT_OWNER" })
	public void testAddRestaurant_NotApprovedUser() throws Exception {
	    mockUser.setApprovalStatus(ApprovalStatus.PENDING);
	    userRepository.save(mockUser);

	    mockMvc.perform(post("/restaurant/add")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(objectMapper.writeValueAsString(sampleRestaurantDto)))
	            .andExpect(status().isForbidden())
	            .andExpect(content().string("Your account is pending admin approval."));
	}



	@Test
	@WithMockUser(username = "owner@example.com")
	public void testGetAllRestaurants() throws Exception {
		mockMvc.perform(get("/restaurant/all")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	@WithMockUser(username = "owner@example.com", roles = { "RESTAURANT_OWNER" })
	public void testGetRestaurantById_Success() throws Exception {
	    mockUser.setApprovalStatus(ApprovalStatus.APPROVED);
	    mockUser.setActive(true);

	    userRepository.saveAndFlush(mockUser);

	    Restaurant savedRestaurant = restaurantRepository.saveAndFlush(
	        new Restaurant(
	            "Pizza Palace",
	            "123 Main St",
	            "9876543210",
	            4.5,
	            "https://logo.com/logo.png",
	            LocalDateTime.now(),
	            "09:00:00",
	            "22:00:00",
	            mockUser
	        )
	    );

	    mockMvc.perform(get("/restaurant/" + savedRestaurant.getRestaurantId())
	            .contentType(MediaType.APPLICATION_JSON))
	            .andDo(print())
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.name").value("Pizza Palace"));
	}

	@Test
	@WithMockUser(username = "owner@example.com", roles = { "RESTAURANT_OWNER" })
	public void testAddRestaurant_MissingFields_ShouldReturnBadRequest() throws Exception {
	    mockUser.setApprovalStatus(ApprovalStatus.APPROVED);
	    userRepository.save(mockUser);

	    RestaurantDto invalidDto = new RestaurantDto(); // empty object
	    String requestBody = objectMapper.writeValueAsString(invalidDto);

	    mockMvc.perform(post("/restaurant/add")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(requestBody))
	            .andExpect(status().isBadRequest());
	}

	@Test
	public void testGetAllRestaurants_ShouldReturnMultiple() throws Exception {
	    restaurantRepository.deleteAll();
	    userRepository.save(mockUser); // ensure user is linked

	    restaurantRepository.saveAll(List.of(
	        new Restaurant("R1", "Address1", "123", 4.1, "logo1", LocalDateTime.now(), "09:00", "22:00", mockUser),
	        new Restaurant("R2", "Address2", "456", 4.8, "logo2", LocalDateTime.now(), "08:00", "20:00", mockUser)
	    ));

	    mockMvc.perform(get("/restaurant/all"))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	public void testGetRestaurantById_NotFound() throws Exception {
	    userRepository.save(mockUser);
	    String token = jwtUtil.generateToken(mockUser.getEmail());

	    mockMvc.perform(get("/restaurant/999999")
	            .header("Authorization", "Bearer " + token)
	            .contentType(MediaType.APPLICATION_JSON))
	            .andExpect(status().isBadRequest());
	}
	

	@Test
	public void testGetRestaurantById_InvalidToken() throws Exception {
	    mockMvc.perform(get("/restaurant/1")
	            .header("Authorization", "Bearer invalid-token"))
	            .andExpect(status().isUnauthorized());
	}


}
