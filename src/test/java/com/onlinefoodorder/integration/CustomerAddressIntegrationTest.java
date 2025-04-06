package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.controller.CustomerAddressController;
import com.onlinefoodorder.dto.CustomerAddressDto;
import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.CustomerAddressRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CustomerAddressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired  // Changed from @Mock to @Autowired for integration test
    private UserRepository userRepository;

    @Autowired  // Changed from @Mock to @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerJwtToken;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // Clear existing data
        customerAddressRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save test user
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("1234567890");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setActive(true);
        testUser.setApprovalStatus(ApprovalStatus.APPROVED);
        testUser = userRepository.save(testUser);  // Save and get managed entity

        // Generate token AFTER user is saved
        customerJwtToken = "Bearer " + jwtUtil.generateToken(testUser.getEmail());
    }

    @Test
    void testGetUserAddresses() throws Exception {
        // Create test address for the user
        CustomerAddress address = new CustomerAddress();
        address.setUser(testUser);
        address.setAddressLine1("123 Main St");
        address.setAddressLine2("Apt 101");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("USA");
        customerAddressRepository.save(address);

        mockMvc.perform(get("/customer/addresses")
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressLine1").value("123 Main St"));
    }

    @Test
    void testAddAddress() throws Exception {
        CustomerAddressDto newAddress = new CustomerAddressDto(
                null, "456 Elm St", "Apt 101", "New York", "NY", "10001", "USA");

        mockMvc.perform(post("/customer/addresses")
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAddress)))
                .andExpect(status().isOk())  // Changed from isCreated() to isOk()
                .andExpect(jsonPath("$.addressLine1").value("456 Elm St"));
    }
    @Test
    void testUpdateAddress() throws Exception {
        // First create a complete address to update
        CustomerAddress existingAddress = new CustomerAddress();
        existingAddress.setUser(testUser);
        existingAddress.setAddressLine1("123 Main St");
        existingAddress.setAddressLine2("Apt 101");
        existingAddress.setCity("New York");
        existingAddress.setState("NY");
        existingAddress.setPostalCode("10001");
        existingAddress.setCountry("USA");
        existingAddress.setDefault(true); // if this field exists
        existingAddress = customerAddressRepository.save(existingAddress);

        // Create update DTO with all required fields
        CustomerAddressDto updatedAddress = new CustomerAddressDto(
                existingAddress.getAddressId(),
                "789 Oak St", 
                "Suite 200", 
                "Los Angeles", 
                "CA", 
                "90001", 
                "USA");

        mockMvc.perform(put("/customer/addresses/{addressId}", existingAddress.getAddressId())
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAddress)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressLine1").value("789 Oak St"))
                .andExpect(jsonPath("$.city").value("Los Angeles"));
    }

    @Test
    void testDeleteAddress() throws Exception {
        // First create a complete address to delete
        CustomerAddress addressToDelete = new CustomerAddress();
        addressToDelete.setUser(testUser);
        addressToDelete.setAddressLine1("123 Main St"); // Required
        addressToDelete.setAddressLine2("Apt 101");
        addressToDelete.setCity("New York"); // Required
        addressToDelete.setState("NY"); // Required
        addressToDelete.setPostalCode("10001"); // Required
        addressToDelete.setCountry("USA"); // Required
        addressToDelete.setDefault(false);
        addressToDelete = customerAddressRepository.save(addressToDelete);

        mockMvc.perform(delete("/customer/addresses/{addressId}", addressToDelete.getAddressId())
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken))
                .andExpect(status().isNoContent());
    }
    @Test
    void testUpdateAddress_Unauthorized() throws Exception {
        // Create and save a different user first
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setUsername("otheruser");
        otherUser.setPassword("password123");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setPhone("0987654321");
        otherUser.setRole(UserRole.CUSTOMER);
        otherUser.setActive(true);
        otherUser = userRepository.save(otherUser);

        // Create a COMPLETE address for the other user
        CustomerAddress otherAddress = new CustomerAddress();
        otherAddress.setUser(otherUser);
        otherAddress.setAddressLine1("123 Other St"); // Required
        otherAddress.setAddressLine2("Apt 2");
        otherAddress.setCity("Other City"); // Required
        otherAddress.setState("OC"); // Required
        otherAddress.setPostalCode("54321"); // Required
        otherAddress.setCountry("Other Country"); // Required
        otherAddress.setDefault(false);
        otherAddress = customerAddressRepository.save(otherAddress);

        // Create a COMPLETE DTO for the update request
        CustomerAddressDto updateDto = new CustomerAddressDto();
        updateDto.setAddressId(otherAddress.getAddressId());
        updateDto.setAddressLine1("Updated Address"); // Required
        updateDto.setAddressLine2("Apt 3");
        updateDto.setCity("Updated City"); // Required
        updateDto.setState("UC"); // Required
        updateDto.setPostalCode("98765"); // Required
        updateDto.setCountry("Updated Country"); // Required

        mockMvc.perform(put("/customer/addresses/{addressId}", otherAddress.getAddressId())
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testAddAddress_MissingRequiredFields() throws Exception {
        CustomerAddressDto invalidAddress = new CustomerAddressDto(
                null, null, null, null, null, null, null);
                
        mockMvc.perform(post("/customer/addresses")
                .header(HttpHeaders.AUTHORIZATION, customerJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAddress)))
                .andExpect(status().isBadRequest());
    }
}