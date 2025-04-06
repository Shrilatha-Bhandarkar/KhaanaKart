package com.onlinefoodorder.integration;

import com.onlinefoodorder.controller.CartController;
import com.onlinefoodorder.dto.CartDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.service.CartService;
import com.onlinefoodorder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CartIntegrationTest {

    @Autowired
    private CartController cartController;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserService userService;

    private final String testUserEmail = "test@example.com";
    private CartDto testCartDto;
    private CartItem testCartItem;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test user and security context
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail(testUserEmail);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(testUserEmail, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Setup test data
        MenuItem testMenuItem = new MenuItem();
        testMenuItem.setItemId(1L);
        testMenuItem.setName("Test Item");
        testMenuItem.setPrice(new BigDecimal("9.99"));

        testCartDto = new CartDto();
        testCartDto.setMenuItemId(testMenuItem.getItemId());
        testCartDto.setQuantity(2);

        testCartItem = new CartItem();
        testCartItem.setCartId(1L);
        testCartItem.setMenuItem(testMenuItem);
        testCartItem.setQuantity(2);
        testCartItem.setAddedAt(LocalDateTime.now());
    }

    @Test
    void addToCart_Integration_Success() {
        // Mock service behavior
        doNothing().when(cartService).addToCart(testUserEmail, testCartDto);
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);

        // Call controller
        ResponseEntity<String> response = cartController.addToCart(testCartDto, SecurityContextHolder.getContext().getAuthentication());

        // Verify
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item added to cart", response.getBody());
        verify(cartService).addToCart(testUserEmail, testCartDto);
    }

    @Test
    void getCart_Integration_Success() {
        // Mock service behavior
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCart(testUserEmail)).thenReturn(cartItems);
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);

        // Call controller
        ResponseEntity<List<CartItem>> response = cartController.getCart(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(testCartItem.getCartId(), response.getBody().get(0).getCartId());
        verify(cartService).getCart(testUserEmail);
    }

    @Test
    void updateCartItem_Integration_Success() {
        // Mock service behavior
        Long cartItemId = 1L;
        doNothing().when(cartService).updateCartItem(cartItemId, testCartDto.getQuantity(), testUserEmail);
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);

        // Call controller
        ResponseEntity<String> response = cartController.updateCartItem(
            cartItemId, 
            testCartDto, 
            SecurityContextHolder.getContext().getAuthentication()
        );

        // Verify
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cart item updated", response.getBody());
        verify(cartService).updateCartItem(cartItemId, testCartDto.getQuantity(), testUserEmail);
    }

    @Test
    void removeFromCart_Integration_Success() {
        // Mock service behavior
        Long cartItemId = 1L;
        doNothing().when(cartService).removeFromCart(cartItemId, testUserEmail);
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);

        // Call controller
        ResponseEntity<String> response = cartController.removeFromCart(
            cartItemId, 
            SecurityContextHolder.getContext().getAuthentication()
        );

        // Verify
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item removed from cart", response.getBody());
        verify(cartService).removeFromCart(cartItemId, testUserEmail);
    }

    @Test
    void addToCart_Integration_ErrorHandling() {
        // Mock service to throw exception
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);
        doThrow(new RuntimeException("Database error")).when(cartService).addToCart(testUserEmail, testCartDto);

        // Call controller
        ResponseEntity<String> response = cartController.addToCart(
            testCartDto, 
            SecurityContextHolder.getContext().getAuthentication()
        );

        // Verify error handling
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Error adding item to cart", response.getBody());
    }

    @Test
    void getCart_Integration_EmptyCart() {
        // Mock empty cart
        when(cartService.getCart(testUserEmail)).thenReturn(List.of());
        when(userService.getUserByEmail(testUserEmail)).thenReturn(testUser);

        // Call controller
        ResponseEntity<List<CartItem>> response = cartController.getCart(
            SecurityContextHolder.getContext().getAuthentication()
        );

        // Verify empty cart handling
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }


}