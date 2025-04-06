package com.onlinefoodorder.exception;

/**
 * Exception thrown when an unauthorized access attempt is detected.
 */
public class UnauthorizedAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
