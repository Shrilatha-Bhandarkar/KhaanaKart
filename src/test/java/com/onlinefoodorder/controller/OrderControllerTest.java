package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.exception.OrderNotFoundException;
import com.onlinefoodorder.service.OrderService;
import com.onlinefoodorder.util.Status.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Principal principal;

    @InjectMocks
    private OrderController orderController;

    private OrderDto orderDto;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        orderDto = new OrderDto();
        orderDto.setOrderId(1L);
        orderDto.setStatus(OrderStatus.PENDING);
    }

    private void setupPrincipal() {
        when(principal.getName()).thenReturn(userEmail);
    }

    @Test
    @Transactional
    void placeOrder_Success() {
        // Arrange
        setupPrincipal();
        when(orderService.placeOrder(eq(userEmail), any(OrderDto.class)))
                .thenReturn(orderDto);

        // Act
        ResponseEntity<OrderDto> response = orderController.placeOrder(principal, orderDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getOrderId());
        verify(orderService).placeOrder(userEmail, orderDto);
    }

    @Test
    void getOrderById_Success() {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(orderDto);

        // Act
        ResponseEntity<OrderDto> response = orderController.getOrderById(1L);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getOrderId());
        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderController.getOrderById(1L);
        });
    }

    @Test
    void getUserOrders_Success() {
        // Arrange
        setupPrincipal();
        List<OrderDto> orders = Collections.singletonList(orderDto);
        when(orderService.getUserOrders(userEmail)).thenReturn(orders);

        // Act
        ResponseEntity<List<OrderDto>> response = orderController.getUserOrders(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(orderService).getUserOrders(userEmail);
    }

    @Test
    @Transactional
    void updateOrderStatus_Success() {
        // Arrange
        setupPrincipal();
        when(orderService.updateOrderStatus(anyLong(), any(OrderStatus.class), eq(userEmail)))
                .thenReturn(orderDto);

        // Use a valid OrderStatus enum value (changed from PROCESSING to PENDING)
        ResponseEntity<OrderDto> response = orderController.updateOrderStatus(
                1L, 
                Map.of("status", "PENDING"), // Changed to valid enum value
                principal
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(orderService).updateOrderStatus(1L, OrderStatus.PENDING, userEmail);
    }

    @Test
    @Transactional
    void applyCouponToOrder_Success() {
        // Arrange
        when(orderService.applyCoupon(1L, "DISCOUNT10")).thenReturn(orderDto);

        // Act
        ResponseEntity<OrderDto> response = orderController.applyCouponToOrder(
                Map.of("orderId", 1L, "couponCode", "DISCOUNT10")
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(orderService).applyCoupon(1L, "DISCOUNT10");
    }

    @Test
    @Transactional
    void removeCouponFromOrder_Success() {
        // Arrange
        OrderDto updatedOrder = new OrderDto();
        updatedOrder.setOrderId(1L);
        when(orderService.removeCoupon(1L)).thenReturn(updatedOrder);

        // Act
        ResponseEntity<String> response = orderController.removeCouponFromOrder(1L);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Coupon removed successfully.", response.getBody());
        verify(orderService).removeCoupon(1L);
    }

    @Test
    void updateOrderStatus_InvalidStatus() {
        // Arrange
        setupPrincipal();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderController.updateOrderStatus(
                    1L,
                    Map.of("status", "INVALID_STATUS"),
                    principal
            );
        });
    }
}