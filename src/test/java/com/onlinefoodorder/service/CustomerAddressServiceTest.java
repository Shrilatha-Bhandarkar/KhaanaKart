package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.CustomerAddressDto;
import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.CustomerAddressRepository;
import com.onlinefoodorder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {

    @Mock
    private CustomerAddressRepository customerAddressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerAddressService customerAddressService;

    private User user;
    private CustomerAddress address;
    private CustomerAddressDto addressDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        address = new CustomerAddress();
        address.setAddressId(1L);
        address.setUser(user);
        address.setAddressLine1("123 Main St");
        address.setCity("Test City");
        address.setState("TS");
        address.setPostalCode("12345");

        addressDto = new CustomerAddressDto();
        addressDto.setAddressLine1("123 Main St");
        addressDto.setCity("Test City");
        addressDto.setState("TS");
        addressDto.setPostalCode("12345");
    }

    @Test
    void getAddressesByUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findByUser(any(User.class))).thenReturn(List.of(address));

        List<CustomerAddressDto> result = customerAddressService.getAddressesByUser("test@example.com");

        assertEquals(1, result.size());
        assertEquals("123 Main St", result.get(0).getAddressLine1());
    }

    @Test
    void getAddressesByUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.getAddressesByUser("test@example.com"));
    }

    @Test
    void addAddress_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenReturn(address);

        CustomerAddressDto result = customerAddressService.addAddress("test@example.com", addressDto);

        assertEquals("123 Main St", result.getAddressLine1());
        verify(customerAddressRepository).save(any(CustomerAddress.class));
    }

    @Test
    void addAddress_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.addAddress("test@example.com", addressDto));
    }

    @Test
    void updateAddress_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenReturn(address);

        addressDto.setAddressLine1("456 Updated St");
        CustomerAddressDto result = customerAddressService.updateAddress("test@example.com", 1L, addressDto);

        assertEquals("456 Updated St", result.getAddressLine1());
        verify(customerAddressRepository).save(any(CustomerAddress.class));
    }

    @Test
    void updateAddress_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.updateAddress("test@example.com", 1L, addressDto));
    }

    @Test
    void updateAddress_AddressNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.updateAddress("test@example.com", 1L, addressDto));
    }

    @Test
    void updateAddress_UnauthorizedUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        address.setUser(otherUser);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        assertThrows(UnauthorizedAccessException.class,
                () -> customerAddressService.updateAddress("test@example.com", 1L, addressDto));
    }

    @Test
    void deleteAddress_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        customerAddressService.deleteAddress(1L, "test@example.com");

        verify(customerAddressRepository).delete(any(CustomerAddress.class));
    }

    @Test
    void deleteAddress_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.deleteAddress(1L, "test@example.com"));
    }

    @Test
    void deleteAddress_AddressNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerAddressService.deleteAddress(1L, "test@example.com"));
    }

    @Test
    void deleteAddress_UnauthorizedUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        address.setUser(otherUser);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerAddressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        assertThrows(UnauthorizedAccessException.class,
                () -> customerAddressService.deleteAddress(1L, "test@example.com"));
    }
}