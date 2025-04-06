package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.ReviewDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ReviewDto testReviewDto;
    private Review testReview;
    private User testUser;
    private Restaurant testRestaurant;
    private final Long testReviewId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("user@example.com");

        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setName("Test Restaurant");

        testReviewDto = new ReviewDto(
        	    testRestaurant.getRestaurantId(), 
        	    4, 
        	    "Great food!"
        	);
        testReview = new Review();
        testReview.setReviewId(testReviewId);
        testReview.setUser(testUser);
        testReview.setRestaurant(testRestaurant);
        testReview.setRating(4);
        testReview.setComment("Great food!");
        testReview.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void addReview_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Review added successfully";
        when(reviewService.addReview(testReviewDto)).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = reviewController.addReview(testReviewDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(successMessage, response.getBody());
        verify(reviewService).addReview(testReviewDto);
    }

    @Test
    void getReviews_ShouldReturnReviewList() {
        // Arrange
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewService.getReviewsByRestaurant(testRestaurant.getRestaurantId())).thenReturn(reviews);

        // Act
        ResponseEntity<List<Review>> response = reviewController.getReviews(testRestaurant.getRestaurantId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        
        Review returnedReview = response.getBody().get(0);
        assertEquals(testReviewId, returnedReview.getReviewId());
        assertEquals(testUser.getUserId(), returnedReview.getUser().getUserId());
        assertEquals(testRestaurant.getRestaurantId(), returnedReview.getRestaurant().getRestaurantId());
        assertEquals(4, returnedReview.getRating());
    }

    @Test
    void deleteReview_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Review deleted successfully";
        when(reviewService.deleteReview(testReviewId)).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = reviewController.deleteReview(testReviewId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(successMessage, response.getBody());
        verify(reviewService).deleteReview(testReviewId);
    }

    @Test
    void addReview_ShouldHandleInvalidRating() {
        // Arrange
        ReviewDto invalidReviewDto = new ReviewDto();
        invalidReviewDto.setRating(6); // Invalid rating
        when(reviewService.addReview(invalidReviewDto))
            .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            reviewController.addReview(invalidReviewDto));
    }

    @Test
    void getReviews_ShouldReturnEmptyListForNoReviews() {
        // Arrange
        when(reviewService.getReviewsByRestaurant(testRestaurant.getRestaurantId())).thenReturn(List.of());

        // Act
        ResponseEntity<List<Review>> response = reviewController.getReviews(testRestaurant.getRestaurantId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }
}