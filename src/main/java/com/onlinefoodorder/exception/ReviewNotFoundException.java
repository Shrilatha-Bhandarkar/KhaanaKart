package com.onlinefoodorder.exception;

/**
 * Exception thrown when a review is not found.
 */
public class ReviewNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
