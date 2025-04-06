package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.DeliveryException;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.repository.OrderRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.util.Status.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Order order;
    private User deliveryPerson;
    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setUserId(1L);
        customer.setRole(UserRole.CUSTOMER);

        deliveryPerson = new User();
        deliveryPerson.setUserId(2L);
        deliveryPerson.setRole(UserRole.DELIVERY_PERSON);

        order = new Order();
        order.setOrderId(1L);
        order.setUser(customer);
        order.setDeliveryPerson(deliveryPerson);
        order.setStatus(OrderStatus.ASSIGNED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAssignedOrders_ShouldReturnAssignedOrders() {
        // Arrange
        when(orderRepository.findByDeliveryPerson_UserIdAndStatus(anyLong(), any(OrderStatus.class)))
                .thenReturn(Collections.singletonList(order));

        // Act
        List<OrderDto> result = deliveryService.getAssignedOrders(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
        assertEquals(OrderStatus.ASSIGNED, result.get(0).getStatus());
        verify(orderRepository, times(1))
                .findByDeliveryPerson_UserIdAndStatus(anyLong(), any(OrderStatus.class));
    }

    @Test
    void getAssignedOrders_WithNullDeliveryPersonId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> deliveryService.getAssignedOrders(null));
    }

    @Test
    void markOrderOutForDelivery_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act
        deliveryService.markOrderOutForDelivery(1L, 2L);

        // Assert
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void markOrderOutForDelivery_WithWrongDeliveryPerson_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(DeliveryException.class, () -> deliveryService.markOrderOutForDelivery(1L, 999L));
    }

    @Test
    void markOrderOutForDelivery_WithWrongStatus_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(DeliveryException.class, () -> deliveryService.markOrderOutForDelivery(1L, 2L));
    }

    @Test
    void markOrderDelivered_ShouldUpdateStatus() {
        // Arrange
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act
        deliveryService.markOrderDelivered(1L, 2L);

        // Assert
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void markOrderDelivered_WithWrongDeliveryPerson_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> deliveryService.markOrderDelivered(1L, 999L));
    }

    @Test
    void markOrderDelivered_WithWrongStatus_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> deliveryService.markOrderDelivered(1L, 2L));
    }

    @Test
    void assignDeliveryPerson_ShouldUpdateOrder() {
        // Arrange
        Long orderId = 1L;
        Long deliveryPersonId = 2L;
        
        Order pendingOrder = new Order();
        pendingOrder.setOrderId(orderId);
        pendingOrder.setStatus(OrderStatus.PENDING);
        
        // Stub with exact IDs that will be used
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById(deliveryPersonId)).thenReturn(Optional.of(deliveryPerson));

        // Act
        deliveryService.assignDeliveryPerson(orderId, deliveryPersonId);

        // Assert
        assertEquals(OrderStatus.ASSIGNED, pendingOrder.getStatus());
        assertEquals(deliveryPerson, pendingOrder.getDeliveryPerson());
        verify(orderRepository).findById(orderId);
        verify(userRepository).findById(deliveryPersonId);
        verify(orderRepository).save(pendingOrder);
    }
    @Test
    void assignDeliveryPerson_WithNonDeliveryUser_ShouldThrowException() {
        // Arrange
        Long orderId = 1L;
        Long invalidUserId = 3L; // Not a delivery person
        
        Order pendingOrder = new Order();
        pendingOrder.setOrderId(orderId);
        pendingOrder.setStatus(OrderStatus.PENDING);
        
        User nonDeliveryUser = new User();
        nonDeliveryUser.setUserId(invalidUserId);
        nonDeliveryUser.setRole(UserRole.CUSTOMER);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.of(nonDeliveryUser));

        // Act & Assert
        assertThrows(DeliveryException.class, 
            () -> deliveryService.assignDeliveryPerson(orderId, invalidUserId));
    }

    @Test
    void assignDeliveryPerson_WithNonExistentOrder_ShouldThrowException() {
        // Arrange
        Long nonExistentOrderId = 999L;
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> deliveryService.assignDeliveryPerson(nonExistentOrderId, 2L));
    }

    @Test
    void assignDeliveryPerson_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        Long orderId = 1L;
        Long nonExistentUserId = 999L;
        
        Order pendingOrder = new Order();
        pendingOrder.setOrderId(orderId);
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> deliveryService.assignDeliveryPerson(orderId, nonExistentUserId));
    }

    @Test
    void getAssignedOrders_ShouldReturnCorrectDtoMapping() {
        // Arrange
        order.setTotalAmount(new BigDecimal("100.0"));  // Note single decimal place
        order.setDeliveryFee(new BigDecimal("10.0"));
        order.setTaxAmount(new BigDecimal("5.0"));
        when(orderRepository.findByDeliveryPerson_UserIdAndStatus(anyLong(), any(OrderStatus.class)))
                .thenReturn(Collections.singletonList(order));

        // Act
        List<OrderDto> result = deliveryService.getAssignedOrders(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        OrderDto dto = result.get(0);
        assertEquals(1L, dto.getOrderId());
        assertEquals(1L, dto.getUserId());
        assertEquals(2L, dto.getDeliveryPersonId());
        
        // Proper BigDecimal comparison ignoring scale
        assertEquals(0, new BigDecimal("100.0").compareTo(dto.getTotalAmount()));
        assertEquals(0, new BigDecimal("10.0").compareTo(dto.getDeliveryFee()));
        assertEquals(0, new BigDecimal("5.0").compareTo(dto.getTaxAmount()));
        assertEquals(OrderStatus.ASSIGNED, dto.getStatus());
    }
    
    @Test
    void getDeliveryChart_ShouldReturnChartData() {
        // Arrange
        String email = "delivery@example.com";
        Object[] row1 = new Object[]{Date.valueOf(LocalDate.now()), 5L};
        Object[] row2 = new Object[]{Date.valueOf(LocalDate.now().minusDays(1)), 3L};
        List<Object[]> mockData = Arrays.asList(row1, row2);
        
        when(orderRepository.getDeliveredOrdersPerDay(email)).thenReturn(mockData);

        // Act
        byte[] result = deliveryService.getDeliveryChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderRepository, times(1)).getDeliveredOrdersPerDay(email);
    }

    @Test
    void getDeliveryChart_WithEmptyData_ShouldReturnChartWithZeroValues() {
        // Arrange
        String email = "delivery@example.com";
        when(orderRepository.getDeliveredOrdersPerDay(email)).thenReturn(Collections.emptyList());

        // Act
        byte[] result = deliveryService.getDeliveryChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderRepository, times(1)).getDeliveredOrdersPerDay(email);
    }

    @Test
    void getTotalDeliveredChart_ShouldReturnChartData() {
        // Arrange
        String email = "delivery@example.com";
        Object[] row = new Object[]{"John Doe", 10L};
        List<Object[]> mockData = Collections.singletonList(row);
        
        when(orderRepository.getTotalOrdersDeliveredByEmail(email, OrderStatus.DELIVERED))
            .thenReturn(mockData);

        // Act
        byte[] result = deliveryService.getTotalDeliveredChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderRepository, times(1))
            .getTotalOrdersDeliveredByEmail(email, OrderStatus.DELIVERED);
    }

    @Test
    void getTotalDeliveredChart_WithNoDeliveries_ShouldReturnZeroChart() {
        // Arrange
        String email = "delivery@example.com";
        when(orderRepository.getTotalOrdersDeliveredByEmail(email, OrderStatus.DELIVERED))
            .thenReturn(Collections.emptyList());

        // Act
        byte[] result = deliveryService.getTotalDeliveredChart(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0); // Chart should still be generated with zero value
        verify(orderRepository, times(1))
            .getTotalOrdersDeliveredByEmail(email, OrderStatus.DELIVERED);
    }

    @Test
    void getAllDeliveryPersonsStatsChart_WithEmptyData_ShouldReturnDefaultChart() {
        // Arrange
        when(orderRepository.getAllDeliveryPersonsStats()).thenReturn(Collections.emptyList());

        // Act
        byte[] result = deliveryService.getAllDeliveryPersonsStatsChart();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(orderRepository, times(1)).getAllDeliveryPersonsStats();
    }

    @Test
    void getAllDeliveryPersonsStatsChart_WithEmptyData_ShouldReturnEmptyChart() {
        // Arrange
        when(orderRepository.getAllDeliveryPersonsStats()).thenReturn(Collections.emptyList());

        // Act
        byte[] result = deliveryService.getAllDeliveryPersonsStatsChart();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0); // Chart should still be generated even with empty data
        verify(orderRepository, times(1)).getAllDeliveryPersonsStats();
    }

    @Test
    void getTotalDeliveredChart_WithNullEmail_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> deliveryService.getTotalDeliveredChart(null));
        
        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    void getDeliveryChart_WithNullEmail_ShouldThrowException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> deliveryService.getDeliveryChart(null));
        
        assertEquals("Email cannot be null", exception.getMessage());
    }
}