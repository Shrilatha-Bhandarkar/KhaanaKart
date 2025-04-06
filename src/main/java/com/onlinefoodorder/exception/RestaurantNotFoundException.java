package com.onlinefoodorder.exception;

/**
 * Exception thrown when a restaurant is not found.
 */
public class RestaurantNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
