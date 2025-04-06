package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.dto.ReviewDto;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.Review;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.ReviewRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private User testUser;
    Restaurant testRestaurant;
    @BeforeEach
    void setUp() {
        // Create a restaurant owner user
        User testOwner = new User(null, "owner@example.com", "testowner", "password", "Jane", "Smith", "9876543210",
                UserRole.RESTAURANT_OWNER, true);
        testOwner.setApprovalStatus(ApprovalStatus.APPROVED);
        testOwner = userRepository.save(testOwner);

        // Create test restaurant with required constructor fields
        testRestaurant = new Restaurant("Test Restaurant", "123 Test Street, Test City", "9876543210", 
                4.5, "https://example.com/logo.png", LocalDateTime.now(), "09:00 AM", "11:00 PM", testOwner);
        
        testRestaurant = restaurantRepository.save(testRestaurant);

        // Create a test customer user for reviews
        testUser = new User(null, "test@example.com", "testuser", "password", "John", "Doe", "1234567890",
                UserRole.CUSTOMER, true);
        testUser.setApprovalStatus(ApprovalStatus.APPROVED);
        testUser = userRepository.save(testUser);

        // Generate JWT token for authentication
        jwtToken = "Bearer " + jwtUtil.generateToken(testUser.getEmail());
    }
    @Test
    void testCreateReview_Success() throws Exception {
        ReviewDto reviewDto = new ReviewDto(testRestaurant.getRestaurantId(), 5, "Great food and service!");

        mockMvc.perform(post("/reviews/add")
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetReviewsByRestaurant_Success() throws Exception {
        mockMvc.perform(get("/reviews/restaurant/1")
                .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isOk());
    }
    @Test
    void testGetReviewsByUser_Success() throws Exception {


        mockMvc.perform(get("/reviews/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))  // Add "Bearer " prefix
                .andExpect(status().isOk())  // Expecting 200 OK response
                .andExpect(jsonPath("$").isArray())  // Expecting response body to be a JSON array
                .andExpect(jsonPath("$[0].restaurantId").exists())  // Expecting a field restaurantId in the response
                .andExpect(jsonPath("$[0].rating").exists())  // Expecting a field rating in the response
                .andExpect(jsonPath("$[0].comment").exists());  // Expecting a field comment in the response
    }



    @Test
    void testUpdateReview_Success() throws Exception {
        Review review = reviewRepository.findAll().stream().findFirst().orElseThrow();
        ReviewDto updatedReview = new ReviewDto(review.getRestaurant().getRestaurantId(), 4, "Updated review!");

        mockMvc.perform(put("/reviews/" + review.getReviewId())
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedReview)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteReview_Success() throws Exception {
        // Ensure a review exists before attempting to delete it
        Review review = new Review(null, testUser, testRestaurant, 5, "Nice place!", LocalDateTime.now());
        review = reviewRepository.save(review);

        mockMvc.perform(delete("/reviews/delete/{reviewId}", review.getReviewId())
                .header(HttpHeaders.AUTHORIZATION, jwtToken)) // Add token
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted successfully!"));
 }



}
