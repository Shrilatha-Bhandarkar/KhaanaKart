package com.onlinefoodorder.integration;

import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserProfile_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setRole(UserRole.CUSTOMER);
        mockUser.setUserId(1L);

        when(userService.getUserByEmail(anyString())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com"))
               .andExpect(header().string("Profile-Path", "/customer/profile"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }
    
    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void getUserProfile_ShouldThrowNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.getUserByEmail(anyString()))
            .thenThrow(new UserNotFoundException("User not found"));  // Match service message
        
        // Act & Assert
        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isNotFound())
               .andExpect(content().string("User not found"));  // Updated expectation
    }

    @Test
    void getUserProfile_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void getUserProfile_ShouldReturnAdminPath_ForAdminRole() throws Exception {
        // Arrange
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.ADMIN);
        
        when(userService.getUserByEmail(anyString())).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(get("/user/profile"))
               .andExpect(header().string("Profile-Path", "/admin/profile"));
    }
}