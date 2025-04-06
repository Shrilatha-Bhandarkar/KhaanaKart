package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.CartDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.CartItemRepository;
import com.onlinefoodorder.repository.CartRepository;
import com.onlinefoodorder.repository.MenuItemRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private MenuItem menuItem;
    private CartDto cartDto;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        menuItem = new MenuItem();
        menuItem.setItemId(1L);
        menuItem.setName("Test Item");
        menuItem.setPrice(new BigDecimal("9.99"));

        cartDto = new CartDto();
        cartDto.setMenuItemId(1L);
        cartDto.setQuantity(2);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        cartItem = new CartItem();
        cartItem.setCartId(1L);
        cartItem.setCart(cart);
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(2);
        cartItem.setAddedAt(LocalDateTime.now());

        cart.setCartItems(new ArrayList<>(List.of(cartItem)));
    }

    @Test
    void addToCart_NewItem_Success() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addToCart("test@example.com", cartDto);

        // Verify save was called twice - once for new cart, once for updated cart
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItem_UpdatesQuantity() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.of(menuItem));
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addToCart("test@example.com", cartDto);

        assertEquals(2, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void addToCart_InvalidMenuItemId_ThrowsException() {
        cartDto.setMenuItemId(null);

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart("test@example.com", cartDto));
    }

    @Test
    void addToCart_InvalidQuantity_ThrowsException() {
        cartDto.setQuantity(0);

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart("test@example.com", cartDto));
    }

    @Test
    void addToCart_MenuItemNotFound_ThrowsException() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addToCart("test@example.com", cartDto));
    }

    @Test
    void getCart_Success() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));

        List<CartItem> result = cartService.getCart("test@example.com");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getMenuItem().getItemId());
    }

    @Test
    void getCart_CartNotFound_ThrowsException() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCart("test@example.com"));
    }

    @Test
    void removeFromCart_Success() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(cartItem));

        cartService.removeFromCart(1L, "test@example.com");

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeFromCart_CartItemNotFound_ThrowsException() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeFromCart(1L, "test@example.com"));
    }

    @Test
    void removeFromCart_UnauthorizedUser_ThrowsException() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(cartItem));

        assertThrows(UnauthorizedAccessException.class,
                () -> cartService.removeFromCart(1L, "unauthorized@example.com"));
    }

    @Test
    void updateCartItem_IncreaseQuantity_Success() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(cartItem));

        cartService.updateCartItem(1L, 3, "test@example.com");

        assertEquals(3, cartItem.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItem_DecreaseToZero_RemovesItem() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(cartItem));

        cartService.updateCartItem(1L, 0, "test@example.com");

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void updateCartItem_CartItemNotFound_ThrowsException() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateCartItem(1L, 2, "test@example.com"));
    }

    @Test
    void updateCartItem_UnauthorizedUser_ThrowsException() {
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(cartItem));

        assertThrows(UnauthorizedAccessException.class,
                () -> cartService.updateCartItem(1L, 2, "unauthorized@example.com"));
    }

    @Test
    void clearCart_Success() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));

        cartService.clearCart("test@example.com");

        verify(cartItemRepository).deleteAll(cart.getCartItems());
    }

    @Test
    void clearCart_CartNotFound_ThrowsException() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.clearCart("test@example.com"));
    }

    @Test
    void calculateTotal_Success() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));

        BigDecimal total = cartService.calculateTotal("test@example.com");

        assertEquals(new BigDecimal("19.98"), total);
    }

    @Test
    void calculateTotal_EmptyCart_ReturnsZero() {
        cart.setCartItems(new ArrayList<>());
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.of(cart));

        BigDecimal total = cartService.calculateTotal("test@example.com");

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void calculateTotal_CartNotFound_ThrowsException() {
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        when(cartRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.calculateTotal("test@example.com"));
    }
}