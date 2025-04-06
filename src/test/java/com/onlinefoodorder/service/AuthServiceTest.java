package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.UserRegistration;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserRegistration customerRegistration;
    private UserRegistration adminRegistration;
    private User savedCustomer;
    private User savedAdmin;

    @BeforeEach
    void setUp() {
        // Setup test data
        customerRegistration = new UserRegistration();
        customerRegistration.setUsername("testcustomer");
        customerRegistration.setPassword("password123");
        customerRegistration.setEmail("customer@test.com");
        customerRegistration.setRole("CUSTOMER");
        customerRegistration.setFirstName("John");
        customerRegistration.setLastName("Doe");
        customerRegistration.setPhone("1234567890");

        adminRegistration = new UserRegistration();
        adminRegistration.setUsername("testadmin");
        adminRegistration.setPassword("admin123");
        adminRegistration.setEmail("admin@test.com");
        adminRegistration.setRole("ADMIN");
        adminRegistration.setFirstName("Admin");
        adminRegistration.setLastName("User");
        adminRegistration.setPhone("9876543210");

        savedCustomer = new User();
        savedCustomer.setUsername("testcustomer");
        savedCustomer.setApprovalStatus(ApprovalStatus.APPROVED);
        savedCustomer.setRole(UserRole.CUSTOMER);

        savedAdmin = new User();
        savedAdmin.setUsername("testadmin");
        savedAdmin.setApprovalStatus(ApprovalStatus.PENDING);
        savedAdmin.setRole(UserRole.ADMIN);
    }

    @Test
    void registerUser_Customer_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedCustomer);

        // Act
        String result = authService.registerUser(customerRegistration);

        // Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).existsByUsername("testcustomer");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_Admin_SuccessWithPendingStatus() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedAdmin);

        // Act
        String result = authService.registerUser(adminRegistration);

        // Assert
        assertEquals("Registration successful. Waiting for admin approval.", result);
        verify(userRepository, times(1)).existsByUsername("testadmin");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_UsernameTaken_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authService.registerUser(customerRegistration));
        
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testcustomer");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_InvalidRole_ThrowsException() {
        // Arrange
        UserRegistration invalidRegistration = new UserRegistration();
        invalidRegistration.setUsername("invaliduser");
        invalidRegistration.setPassword("password123");
        invalidRegistration.setRole("invalidrole");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(invalidRegistration));
    }

    @Test
    void registerUser_EncodesPassword() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword", user.getPassword());
            return savedCustomer;
        });

        // Act
        authService.registerUser(customerRegistration);

        // Assert - Verification happens in the mock answer above
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void registerUser_SetsCorrectCustomerStatus() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(ApprovalStatus.APPROVED, user.getApprovalStatus());
            return savedCustomer;
        });

        // Act
        authService.registerUser(customerRegistration);

        // Assert - Verification happens in the mock answer above
    }

    @Test
    void registerUser_SetsCorrectAdminStatus() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(ApprovalStatus.PENDING, user.getApprovalStatus());
            return savedAdmin;
        });

        // Act
        authService.registerUser(adminRegistration);

        // Assert - Verification happens in the mock answer above
    }

    @Test
    void registerUser_SetsActiveTrue() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertTrue(user.isActive());
            return savedCustomer;
        });

        // Act
        authService.registerUser(customerRegistration);

        // Assert - Verification happens in the mock answer above
    }
}