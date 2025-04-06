package com.onlinefoodorder.controller;

import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail(testEmail);
        testUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    void getUserProfile_ShouldReturnUserWithCorrectHeaders() {
        // Arrange
        when(principal.getName()).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.getUserProfile(principal);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testUser, response.getBody());
        
        HttpHeaders headers = response.getHeaders();
        assertEquals("/customer/profile", headers.getFirst("Profile-Path"));
        
        verify(userService, times(1)).getUserByEmail(testEmail);
    }

    @Test
    void getUserProfile_WithAdminRole_ShouldReturnAdminPath() {
        // Arrange
        testUser.setRole(UserRole.ADMIN);
        when(principal.getName()).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.getUserProfile(principal);

        // Assert
        assertEquals("/admin/profile", response.getHeaders().getFirst("Profile-Path"));
    }

    @Test
    void getUserProfile_WithNullRole_ShouldReturnDefaultPath() {
        // Arrange
        testUser.setRole(null);
        when(principal.getName()).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.getUserProfile(principal);

        // Assert
        assertEquals("/user/profile", response.getHeaders().getFirst("Profile-Path"));
    }

    @Test
    void getUserProfile_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(principal.getName()).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userController.getUserProfile(principal);
        });
        
        verify(userService, times(1)).getUserByEmail(testEmail);
    }

    @Test
    void getUserProfile_WithDeliveryPersonRole_ShouldReturnDeliveryPath() {
        // Arrange
        testUser.setRole(UserRole.DELIVERY_PERSON);
        when(principal.getName()).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.getUserProfile(principal);

        // Assert
        assertEquals("/delivery/profile", response.getHeaders().getFirst("Profile-Path"));
    }
}