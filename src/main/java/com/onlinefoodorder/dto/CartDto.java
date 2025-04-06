package com.onlinefoodorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO representing a cart item.
 */
public class CartDto {
	@NotNull
    private Long cartId;

    @NotNull
    private Long menuItemId;

    @Min(1)
    private int quantity;

	public Long getCartId() {
		return cartId;
	}

	public void setCartId(Long cartId) {
		this.cartId = cartId;
	}

	public Long getMenuItemId() {
		return menuItemId;
	}

	public void setMenuItemId(Long menuItemId) {
		this.menuItemId = menuItemId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public CartDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CartDto(Long cartId, Long menuItemId, int quantity) {
		super();
		this.cartId = cartId;
		this.menuItemId = menuItemId;
		this.quantity = quantity;
	}

}
