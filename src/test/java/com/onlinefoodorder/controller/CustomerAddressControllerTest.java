package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.CustomerAddressDto;
import com.onlinefoodorder.service.CustomerAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAddressControllerTest {

    @Mock
    private CustomerAddressService addressService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CustomerAddressController addressController;

    private final String testUserEmail = "test@example.com";
    private CustomerAddressDto testAddressDto;

    @BeforeEach
    void setUp() {
        testAddressDto = new CustomerAddressDto();
        testAddressDto.setAddressId(1L);
        testAddressDto.setAddressLine1("123 Main St");
        testAddressDto.setCity("Test City");
        testAddressDto.setState("TS");
        testAddressDto.setPostalCode("12345");
        testAddressDto.setCountry("Test Country");
    }

    @Test
    void getUserAddresses_ShouldReturnAddressList() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        List<CustomerAddressDto> addresses = Arrays.asList(testAddressDto);
        when(addressService.getAddressesByUser(testUserEmail)).thenReturn(addresses);

        // Act
        ResponseEntity<List<CustomerAddressDto>> response = addressController.getUserAddresses(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(testAddressDto.getAddressId(), response.getBody().get(0).getAddressId());
        verify(addressService).getAddressesByUser(testUserEmail);
    }

    @Test
    void addAddress_ShouldReturnCreatedAddress() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        when(addressService.addAddress(testUserEmail, testAddressDto)).thenReturn(testAddressDto);

        // Act
        ResponseEntity<CustomerAddressDto> response = 
            addressController.addAddress(principal, testAddressDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testAddressDto, response.getBody());
        verify(addressService).addAddress(testUserEmail, testAddressDto);
    }

    @Test
    void updateAddress_ShouldReturnUpdatedAddress() {
        // Arrange
        Long addressId = 1L;
        when(principal.getName()).thenReturn(testUserEmail);
        when(addressService.updateAddress(testUserEmail, addressId, testAddressDto))
            .thenReturn(testAddressDto);

        // Act
        ResponseEntity<CustomerAddressDto> response = 
            addressController.updateAddress(principal, addressId, testAddressDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testAddressDto, response.getBody());
        verify(addressService).updateAddress(testUserEmail, addressId, testAddressDto);
    }

    @Test
    void deleteAddress_ShouldReturnNoContent() {
        // Arrange
        Long addressId = 1L;
        when(principal.getName()).thenReturn(testUserEmail);
        doNothing().when(addressService).deleteAddress(addressId, testUserEmail);

        // Act
        ResponseEntity<Void> response = 
            addressController.deleteAddress(principal, addressId);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(addressService).deleteAddress(addressId, testUserEmail);
    }

    @Test
    void getUserAddresses_ShouldHandleEmptyList() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        when(addressService.getAddressesByUser(testUserEmail)).thenReturn(List.of());

        // Act
        ResponseEntity<List<CustomerAddressDto>> response = 
            addressController.getUserAddresses(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }
}