package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Test
    void testGetDashboardStats() {
        // Mock repository responses
        when(userRepository.count()).thenReturn(10L);
        when(restaurantRepository.count()).thenReturn(5L);
        when(orderRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(20L);
        when(orderRepository.findByCreatedAtAfter(any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(orderItemRepository.findTopSellingItems())
            .thenReturn(Collections.emptyList());
        when(orderRepository.findMostActiveUsers())
            .thenReturn(Collections.emptyList());
        when(orderRepository.findTopRestaurantsByOrderCount())
            .thenReturn(Collections.emptyList());
        when(orderRepository.findTopRestaurantsByRevenue())
            .thenReturn(Collections.emptyList());

        DashboardStatsDto stats = adminDashboardService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalUsers());
        assertEquals(5L, stats.getTotalRestaurants());
        assertEquals(20L, stats.getTotalOrdersToday());
        assertEquals(BigDecimal.ZERO, stats.getTotalRevenueThisMonth());
        assertTrue(stats.getTopSellingItems().isEmpty());
        assertTrue(stats.getMostActiveUsers().isEmpty());
        assertTrue(stats.getTopRestaurantsByOrders().isEmpty());
        assertTrue(stats.getTopRestaurantsByRevenue().isEmpty());
    }

    @Test
    void testGetTodayOrderCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        when(orderRepository.countByCreatedAtAfter(startOfDay)).thenReturn(15L);
        
        long count = adminDashboardService.getTodayOrderCount();
        
        assertEquals(15L, count);
        verify(orderRepository).countByCreatedAtAfter(startOfDay);
    }

    @Test
    void testGetMonthlyRevenue() {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        when(orderRepository.findByCreatedAtAfter(startOfMonth))
            .thenReturn(Collections.emptyList());
        
        BigDecimal revenue = adminDashboardService.getMonthlyRevenue();
        
        assertEquals(BigDecimal.ZERO, revenue);
        verify(orderRepository).findByCreatedAtAfter(startOfMonth);
    }

    @Test
    void testGetGeneralStats() {
        when(userRepository.count()).thenReturn(10L);
        when(orderRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(20L);
        when(orderRepository.findByCreatedAtAfter(any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(orderItemRepository.findTopSellingItems())
            .thenReturn(Collections.emptyList());
        when(orderRepository.findMostActiveUsers())
            .thenReturn(Collections.emptyList());

        Map<String, Object> generalStats = adminDashboardService.getGeneralStats();

        assertNotNull(generalStats);
        assertEquals(10L, generalStats.get("totalUsers"));
        assertEquals(20L, generalStats.get("totalOrdersToday"));
        assertEquals(BigDecimal.ZERO, generalStats.get("totalRevenueThisMonth"));
        assertNotNull(generalStats.get("topSellingItems"));
        assertNotNull(generalStats.get("mostActiveUsers"));
    }

    @Test
    void testGetRestaurantStats() {
        when(restaurantRepository.count()).thenReturn(5L);
        when(orderRepository.findTopRestaurantsByOrderCount())
            .thenReturn(Collections.emptyList());
        when(orderRepository.findTopRestaurantsByRevenue())
            .thenReturn(Collections.emptyList());

        Map<String, Object> restaurantStats = adminDashboardService.getRestaurantStats();

        assertNotNull(restaurantStats);
        assertEquals(5L, restaurantStats.get("totalRestaurants"));
        assertNotNull(restaurantStats.get("topRestaurantsByOrders"));
        assertNotNull(restaurantStats.get("topRestaurantsByRevenue"));
    }

    @Test
    void testGetChartMethods() {
        // Setup mock data with non-empty values
        DashboardStatsDto.ItemStat mockItem = new DashboardStatsDto.ItemStat("Pizza", 10);
        DashboardStatsDto.UserStat mockUser = new DashboardStatsDto.UserStat("john_doe", 5);
        DashboardStatsDto.RestaurantStat mockRestaurant = 
            new DashboardStatsDto.RestaurantStat("Pizza Place", new BigDecimal("1000.00"));

        when(userRepository.count()).thenReturn(1L);
        when(restaurantRepository.count()).thenReturn(1L);
        when(orderRepository.countByCreatedAtAfter(any())).thenReturn(1L);
        when(orderRepository.findByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        
        // Return non-empty lists for chart data
        when(orderItemRepository.findTopSellingItems())
            .thenReturn(Collections.singletonList(mockItem));
        when(orderRepository.findMostActiveUsers())
            .thenReturn(Collections.singletonList(mockUser));
        when(orderRepository.findTopRestaurantsByRevenue())
            .thenReturn(Collections.singletonList(mockRestaurant));

        // Test chart methods
        byte[] itemsChart = adminDashboardService.getTopSellingItemsChart();
        byte[] usersChart = adminDashboardService.getActiveUsersChart();
        byte[] revenueChart = adminDashboardService.getRestaurantRevenueChart();

        assertNotNull(itemsChart);
        assertTrue(itemsChart.length > 0);
        
        assertNotNull(usersChart);
        assertTrue(usersChart.length > 0);
        
        assertNotNull(revenueChart);
        assertTrue(revenueChart.length > 0);
    }
}