package com.onlinefoodorder.exception;

/**
 * Exception thrown when an order is not found.
 */
public class OrderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(String message) {
        super(message);
    }
}
