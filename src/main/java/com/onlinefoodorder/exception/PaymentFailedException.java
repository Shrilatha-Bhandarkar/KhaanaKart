package com.onlinefoodorder.exception;

/**
 * Exception thrown when a payment processing fails.
 */
public class PaymentFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PaymentFailedException(String message) {
        super(message);
    }
}
