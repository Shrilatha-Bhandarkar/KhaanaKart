package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.CartDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CartController cartController;

    private final String testUserEmail = "test@example.com";
    private CartDto testCartDto;
    private CartItem testCartItem;
    private Cart testCart;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCart = new Cart();
        testCart.setId(1L);
        
        testMenuItem = new MenuItem();
        testMenuItem.setItemId(1L);
        testMenuItem.setName("Test Item");
        testMenuItem.setPrice(new BigDecimal("9.99"));

        testCartDto = new CartDto();
        testCartDto.setMenuItemId(testMenuItem.getItemId()); // Only set menuItemId in DTO
        testCartDto.setQuantity(2);

        testCartItem = new CartItem();
        testCartItem.setCartId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setMenuItem(testMenuItem);
        testCartItem.setQuantity(2);
        testCartItem.setAddedAt(LocalDateTime.now());
    }

    @Test
    void addToCart_ShouldReturnSuccess() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        doNothing().when(cartService).addToCart(testUserEmail, testCartDto);

        // Act
        ResponseEntity<String> response = cartController.addToCart(testCartDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item added to cart", response.getBody());
        verify(cartService).addToCart(testUserEmail, testCartDto);
    }

    @Test
    void getCart_ShouldReturnCartItems() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCart(testUserEmail)).thenReturn(cartItems);

        // Act
        ResponseEntity<List<CartItem>> response = cartController.getCart(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        CartItem returnedItem = response.getBody().get(0);
        assertEquals(testCartItem.getCartId(), returnedItem.getCartId());
        assertEquals(testMenuItem.getItemId(), returnedItem.getMenuItem().getItemId());
        assertEquals(2, returnedItem.getQuantity());
        assertNotNull(returnedItem.getAddedAt());
    }

    @Test
    void updateCartItem_ShouldReturnSuccess() {
        // Arrange
        Long cartItemId = 1L;
        when(principal.getName()).thenReturn(testUserEmail);
        doNothing().when(cartService).updateCartItem(cartItemId, testCartDto.getQuantity(), testUserEmail);

        // Act
        ResponseEntity<String> response = cartController.updateCartItem(cartItemId, testCartDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cart item updated", response.getBody());
        verify(cartService).updateCartItem(cartItemId, testCartDto.getQuantity(), testUserEmail);
    }

    @Test
    void removeFromCart_ShouldReturnSuccess() {
        // Arrange
        Long cartItemId = 1L;
        when(principal.getName()).thenReturn(testUserEmail);
        doNothing().when(cartService).removeFromCart(cartItemId, testUserEmail);

        // Act
        ResponseEntity<String> response = cartController.removeFromCart(cartItemId, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item removed from cart", response.getBody());
        verify(cartService).removeFromCart(cartItemId, testUserEmail);
    }

    @Test
    void addToCart_ShouldHandleException() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        doThrow(new RuntimeException("Database error")).when(cartService).addToCart(testUserEmail, testCartDto);

        // Act
        ResponseEntity<String> response = cartController.addToCart(testCartDto, principal);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Error adding item to cart", response.getBody());
    }

    @Test
    void getCart_ShouldHandleEmptyCart() {
        // Arrange
        when(principal.getName()).thenReturn(testUserEmail);
        when(cartService.getCart(testUserEmail)).thenReturn(List.of());

        // Act
        ResponseEntity<List<CartItem>> response = cartController.getCart(principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
    }
}