package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.AuthRequest;
import com.onlinefoodorder.dto.AuthResponse;
import com.onlinefoodorder.dto.UserRegistration;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.service.AuthService;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private AuthRequest validAuthRequest;
    private AuthRequest invalidAuthRequest;
    private UserRegistration validRegistration;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setApprovalStatus(ApprovalStatus.APPROVED);

        validAuthRequest = new AuthRequest();
        validAuthRequest.setEmail("test@example.com");
        validAuthRequest.setPassword("password");

        invalidAuthRequest = new AuthRequest();
        invalidAuthRequest.setEmail("test@example.com");
        invalidAuthRequest.setPassword("wrongpassword");

        validRegistration = new UserRegistration();
        validRegistration.setEmail("new@example.com");
        validRegistration.setPassword("password");
        validRegistration.setFirstName("John");
        validRegistration.setLastName("Doe");
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("valid.jwt.token");

        // Act
        ResponseEntity<AuthResponse> response = authController.login(validAuthRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("valid.jwt.token", response.getBody().getToken());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void login_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            authController.login(invalidAuthRequest);
        });
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            authController.login(validAuthRequest);
        });
    }

    @Test
    void login_UserNotApproved() {
        // Arrange
        testUser.setApprovalStatus(ApprovalStatus.PENDING);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            authController.login(validAuthRequest);
        });
        
        assertEquals("Account not approved. Contact admin.", exception.getMessage());
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(authService.registerUser(any(UserRegistration.class))).thenReturn("Registration successful");

        // Act
        ResponseEntity<String> response = authController.registerUser(validRegistration);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Registration successful", response.getBody());
        verify(authService, times(1)).registerUser(any(UserRegistration.class));
    }

    @Test
    void registerUser_Failure() {
        // Arrange
        when(authService.registerUser(any(UserRegistration.class))).thenThrow(new RuntimeException("Registration failed"));

        // Act
        ResponseEntity<String> response = authController.registerUser(validRegistration);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Registration failed. Please try again.", response.getBody());
    }

    @Test
    void logout_Success() {
        // Act
        ResponseEntity<String> response = authController.logout();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Logout successful", response.getBody());
    }
}