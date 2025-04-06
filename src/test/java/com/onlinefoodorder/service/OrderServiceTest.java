package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.OrderDto;
import com.onlinefoodorder.dto.OrderItemDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.exception.*;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.Status.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CustomerAddressRepository addressRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private CouponRepository couponRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Restaurant restaurant;
    private CustomerAddress address;
    private MenuItem menuItem;
    private OrderDto orderDto;
    private Order order;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setOwner(user);

        address = new CustomerAddress();
        address.setAddressId(1L);
        address.setUser(user);

        menuItem = new MenuItem();
        menuItem.setItemId(1L);
        menuItem.setRestaurant(restaurant);
        menuItem.setPrice(new BigDecimal("10.00"));

        order = new Order();
        order.setOrderId(1L);
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setTaxAmount(new BigDecimal("1.00"));
        order.setDeliveryFee(new BigDecimal("5.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1L);
        orderItem.setOrder(order);  // Set the order reference
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(2);
        orderItem.setPrice(new BigDecimal("20.00"));
        order.setOrderItems(List.of(orderItem));
        
        orderDto = new OrderDto();
        orderDto.setOrderId(1L);
        orderDto.setUserId(1L);
        orderDto.setRestaurantId(1L);
        orderDto.setDeliveryAddressId(1L);
        orderDto.setStatus(OrderStatus.PENDING);
        orderDto.setTotalAmount(new BigDecimal("20.00"));
        orderDto.setTaxAmount(new BigDecimal("1.00"));
        orderDto.setDeliveryFee(new BigDecimal("5.00"));
        orderDto.setCreatedAt(LocalDateTime.now());
        orderDto.setUpdatedAt(LocalDateTime.now());
        
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setMenuItemId(1L);
        itemDto.setQuantity(2);
        orderDto.setOrderItems(List.of(itemDto));
        // Set order items
        order.setOrderItems(List.of(orderItem));

        coupon = new Coupon();
        coupon.setCode("TEST10");
        coupon.setRestaurant(restaurant);
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(new BigDecimal("10"));
        coupon.setMinOrderValue(new BigDecimal("15.00"));
        coupon.setMaxDiscount(new BigDecimal("5.00"));
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);
    }

    @Test
    void placeOrder_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.placeOrder("test@example.com", orderDto);

        assertNotNull(result);
        assertEquals(1L, result.getRestaurantId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@example.com", orderDto));
    }

    @Test
    void placeOrder_RestaurantNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@example.com", orderDto));
    }

    @Test
    void placeOrder_AddressNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@example.com", orderDto));
    }

    @Test
    void placeOrder_MenuItemNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder("test@example.com", orderDto));
    }

    @Test
    void placeOrder_MenuItemWrongRestaurant_ThrowsException() {
        Restaurant otherRestaurant = new Restaurant();
        otherRestaurant.setRestaurantId(2L);
        menuItem.setRestaurant(otherRestaurant);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder("test@example.com", orderDto));
    }

    

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    @Test
    void getUserOrders_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(any(User.class))).thenReturn(List.of(order));

        List<OrderDto> result = orderService.getUserOrders("test@example.com");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
    }

    @Test
    void updateOrderStatus_Success_RestaurantOwner() {
        // 1. Configure mocks
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setStatus(OrderStatus.PREPARING); // Simulate status update
            return savedOrder;
        });

        // 2. Execute the test
        OrderDto result = orderService.updateOrderStatus(1L, OrderStatus.PREPARING, "owner@example.com");

        // 3. Verify results
        assertNotNull(result, "Result should not be null");
        assertEquals(OrderStatus.PREPARING, result.getStatus(), "Status should be updated");
        assertEquals(1L, result.getOrderId(), "Order ID should match");
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderStatus_UnauthorizedUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherUser));

        assertThrows(UnauthorizedAccessException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.PREPARING, "other@example.com"));
    }

    @Test
    void applyCoupon_Success() {
        orderDto.setCouponCode("TEST10");
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(coupon));

        OrderDto result = orderService.applyCoupon(1L, "TEST10");

        assertNotNull(result.getDiscountAmount());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void applyCoupon_InvalidCoupon_ThrowsException() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.applyCoupon(1L, "INVALID"));
    }

    @Test
    void removeCoupon_Success() {
        order.setCoupon(coupon);
        order.setDiscountAmount(new BigDecimal("2.00"));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        OrderDto result = orderService.removeCoupon(1L);

        assertNull(result.getCouponCode());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        verify(orderRepository).save(any(Order.class));
    }
}