package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.ReviewDto;
import com.onlinefoodorder.entity.Review;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.ReviewNotFoundException;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.repository.ReviewRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Restaurant testRestaurant;
    private ReviewDto testReviewDto;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUserId(1L);

        testRestaurant = new Restaurant();
        testRestaurant.setRestaurantId(1L);
        testRestaurant.setRating(0.0);

        testReviewDto = new ReviewDto();
        testReviewDto.setRestaurantId(1L);
        testReviewDto.setRating(5);
        testReviewDto.setComment("Great food!");

        testReview = new Review();
        testReview.setReviewId(1L);
        testReview.setUser(testUser);
        testReview.setRestaurant(testRestaurant);
        testReview.setRating(5);
        testReview.setComment("Great food!");
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void addReview_Success() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        String result = reviewService.addReview(testReviewDto);

        // Assert
        assertEquals("Review added successfully!", result);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    void addReview_UserNotFound() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.addReview(testReviewDto));
    }

    @Test
    void addReview_RestaurantNotFound() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.addReview(testReviewDto));
    }

    @Test
    void getReviewsByRestaurant_Success() {
        // Arrange
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(Collections.singletonList(testReview));

        // Act
        List<Review> reviews = reviewService.getReviewsByRestaurant(1L);

        // Assert
        assertEquals(1, reviews.size());
        assertEquals(testReview, reviews.get(0));
    }

    @Test
    void getReviewsByRestaurant_EmptyList() {
        // Arrange
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Review> reviews = reviewService.getReviewsByRestaurant(1L);

        // Assert
        assertTrue(reviews.isEmpty());
    }

    @Test
    void deleteReview_Success() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        
        // Mock the reviews list that will be used to calculate the new rating
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(Collections.emptyList());

        // Act
        String result = reviewService.deleteReview(1L);

        // Assert
        assertEquals("Review deleted successfully!", result);
        verify(reviewRepository, times(1)).delete(testReview);
        verify(restaurantRepository, times(1)).save(testRestaurant);
        // Verify the rating was updated to 0.0 since we returned empty list
        assertEquals(0.0, testRestaurant.getRating());
    }
    @Test
    void deleteReview_UserNotFound() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> reviewService.deleteReview(1L));
    }

    @Test
    void deleteReview_ReviewNotFound() {
        // Arrange
        setupSecurityContext();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReviewNotFoundException.class, () -> reviewService.deleteReview(1L));
    }

    @Test
    void deleteReview_UnauthorizedUser() {
        // Arrange
        setupSecurityContext();
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        
        Review anotherReview = new Review();
        anotherReview.setReviewId(2L);
        anotherReview.setUser(anotherUser);
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(anotherReview));

        // Act & Assert
        assertThrows(SecurityException.class, () -> reviewService.deleteReview(2L));
    }

    @Test
    void updateRestaurantRating_Success() {
        // Arrange
        Review review1 = new Review();
        review1.setRating(4);
        Review review2 = new Review();
        review2.setRating(5);
        
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(List.of(review1, review2));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // Act
        reviewService.updateRestaurantRating(1L);

        // Assert
        assertEquals(4.5, testRestaurant.getRating());
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    void updateRestaurantRating_NoReviews() {
        // Arrange
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(Collections.emptyList());
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // Act
        reviewService.updateRestaurantRating(1L);

        // Assert
        assertEquals(0.0, testRestaurant.getRating());
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    void updateRestaurantRating_RestaurantNotFound() {
        // Arrange
        when(reviewRepository.findByRestaurantRestaurantId(1L)).thenReturn(Collections.singletonList(testReview));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.updateRestaurantRating(1L));
    }
}