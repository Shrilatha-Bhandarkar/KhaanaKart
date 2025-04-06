package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.service.AdminDashboardService;
import com.onlinefoodorder.service.DeliveryService;
import com.onlinefoodorder.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminDashboardService adminDashboardService;

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private AdminController adminController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setApprovalStatus(Status.ApprovalStatus.PENDING);
    }

    @Test
    void approveUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<String> response = adminController.approveUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User approved successfully.", response.getBody());
        assertEquals(Status.ApprovalStatus.APPROVED, testUser.getApprovalStatus());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void approveUser_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminController.approveUser(1));
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<String> response = adminController.rejectUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User rejected successfully.", response.getBody());
        assertEquals(Status.ApprovalStatus.REJECTED, testUser.getApprovalStatus());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void rejectUser_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminController.rejectUser(1));
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignDeliveryPerson_Success() {
        doNothing().when(deliveryService).assignDeliveryPerson(anyLong(), anyLong());

        ResponseEntity<String> response = adminController.assignDeliveryPerson(100L, 50L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delivery person assigned successfully.", response.getBody());
        verify(deliveryService, times(1)).assignDeliveryPerson(100L, 50L);
    }

    @Test
    void getGeneralStats_Success() {
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalUsers", 100);
        mockStats.put("activeOrders", 25);
        
        when(adminDashboardService.getGeneralStats()).thenReturn(mockStats);

        ResponseEntity<Map<String, Object>> response = adminController.getGeneralStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockStats, response.getBody());
        verify(adminDashboardService, times(1)).getGeneralStats();
    }

    @Test
    void getRestaurantStats_Success() {
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalRestaurants", 15);
        mockStats.put("activeRestaurants", 12);
        
        when(adminDashboardService.getRestaurantStats()).thenReturn(mockStats);

        ResponseEntity<Map<String, Object>> response = adminController.getRestaurantStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockStats, response.getBody());
        verify(adminDashboardService, times(1)).getRestaurantStats();
    }

    @Test
    void getTopSellingItemsChartImage_Success() {
        byte[] mockImage = new byte[]{1, 2, 3};
        when(adminDashboardService.getTopSellingItemsChart()).thenReturn(mockImage);

        ResponseEntity<byte[]> response = adminController.getTopSellingItemsChartImage();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockImage, response.getBody());
        assertEquals("image/png", response.getHeaders().getFirst("Content-Type"));
        assertEquals("inline; filename=\"top-selling-items.png\"", 
            response.getHeaders().getFirst("Content-Disposition"));
        verify(adminDashboardService, times(1)).getTopSellingItemsChart();
    }

    @Test
    void getActiveUsersChartImage_Success() {
        byte[] mockImage = new byte[]{4, 5, 6};
        when(adminDashboardService.getActiveUsersChart()).thenReturn(mockImage);

        ResponseEntity<byte[]> response = adminController.getActiveUsersChartImage();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockImage, response.getBody());
        assertEquals("image/png", response.getHeaders().getFirst("Content-Type"));
        assertEquals("inline; filename=\"active-users.png\"", 
            response.getHeaders().getFirst("Content-Disposition"));
        verify(adminDashboardService, times(1)).getActiveUsersChart();
    }

    @Test
    void getRestaurantRevenueChartImage_Success() {
        byte[] mockImage = new byte[]{7, 8, 9};
        when(adminDashboardService.getRestaurantRevenueChart()).thenReturn(mockImage);

        ResponseEntity<byte[]> response = adminController.getRestaurantRevenueChartImage();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockImage, response.getBody());
        assertEquals("image/png", response.getHeaders().getFirst("Content-Type"));
        assertEquals("inline; filename=\"restaurant-revenue.png\"", 
            response.getHeaders().getFirst("Content-Disposition"));
        verify(adminDashboardService, times(1)).getRestaurantRevenueChart();
    }

    @Test
    void getDashboardStats_Success() {
        DashboardStatsDto mockStats = new DashboardStatsDto();
        mockStats.setTotalUsers(100);
        
        when(adminDashboardService.getDashboardStats()).thenReturn(mockStats);

        ResponseEntity<DashboardStatsDto> response = adminController.getDashboardStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockStats, response.getBody());
        verify(adminDashboardService, times(1)).getDashboardStats();
    }
}