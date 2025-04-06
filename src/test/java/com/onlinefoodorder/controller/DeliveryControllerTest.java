package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.service.DeliveryService;
import com.onlinefoodorder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock(lenient = true)  // Added lenient to prevent strict stubbing issues
    private DeliveryService deliveryService;

    @Mock(lenient = true)
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock(lenient = true)
    private Principal principal;

    @InjectMocks
    private DeliveryController deliveryController;
    
    @Mock
    private Authentication authentication;


    private byte[] mockChartImage;
    private User testUser;
    private final Long deliveryPersonId = 1L;
    private final String userEmail = "delivery@example.com";

    @BeforeEach
    void setUp() {
        mockChartImage = new byte[]{0x12, 0x34, 0x56}; // Sample image bytes
        testUser = new User();
        testUser.setEmail(userEmail);
        
    }
    private void setupUserIdConversion() {
        when(userService.getUserIdFromPrincipal(principal)).thenReturn(deliveryPersonId);
    }

    @Test
    void getAssignedOrders_Success() {
        // Arrange
    	 when(principal.getName()).thenReturn(userEmail); // Move stubbing here
        setupUserIdConversion();
        OrderDto order = new OrderDto();
        when(deliveryService.getAssignedOrders(deliveryPersonId))
            .thenReturn(List.of(order));

        // Act
        ResponseEntity<List<OrderDto>> response = deliveryController.getAssignedOrders(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(deliveryService).getAssignedOrders(deliveryPersonId);
    }

    @Test
    void getAssignedOrders_EmptyList() {
        // Arrange
        setupUserIdConversion();
        when(deliveryService.getAssignedOrders(deliveryPersonId))
            .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<OrderDto>> response = deliveryController.getAssignedOrders(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @Transactional
    void markOrderOutForDelivery_Success() {
        // Arrange
        setupUserIdConversion();
        Long orderId = 1L;
        doNothing().when(deliveryService).markOrderOutForDelivery(orderId, deliveryPersonId);

        // Act
        ResponseEntity<String> response = deliveryController.markOrderOutForDelivery(orderId, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Order marked as Out for Delivery", response.getBody());
        verify(deliveryService).markOrderOutForDelivery(orderId, deliveryPersonId);
    }

    @Test
    @Transactional
    void markOrderDelivered_Success() {
        // Arrange
        setupUserIdConversion();
        Long orderId = 1L;
        doNothing().when(deliveryService).markOrderDelivered(orderId, deliveryPersonId);

        // Act
        ResponseEntity<String> response = deliveryController.markOrderDelivered(orderId, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Order marked as Delivered", response.getBody());
        verify(deliveryService).markOrderDelivered(orderId, deliveryPersonId);
    }
    
    @Test
    void getDeliveryChart_Success() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(deliveryService.getDeliveryChart(userEmail)).thenReturn(mockChartImage);

        // Act
        ResponseEntity<byte[]> response = deliveryController.getDeliveryChart(authentication);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockChartImage, response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getDeliveryChart_EmptyResult() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(deliveryService.getDeliveryChart(userEmail)).thenReturn(new byte[0]);

        // Act
        ResponseEntity<byte[]> response = deliveryController.getDeliveryChart(authentication);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getTotalDeliveriesChart_Success() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(deliveryService.getTotalDeliveredChart(userEmail)).thenReturn(mockChartImage);

        // Act
        ResponseEntity<byte[]> response = deliveryController.getTotalDeliveriesChart(authentication);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockChartImage, response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getTotalDeliveriesChart_UserNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            deliveryController.getTotalDeliveriesChart(authentication));
    }

    @Test
    void getTotalDeliveriesChart_EmptyResult() {
        // Arrange
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(deliveryService.getTotalDeliveredChart(userEmail)).thenReturn(new byte[0]);

        // Act
        ResponseEntity<byte[]> response = deliveryController.getTotalDeliveriesChart(authentication);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }
}