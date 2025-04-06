package com.onlinefoodorder.util;

public class Status {
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PREPARING,
        ASSIGNED,
        READY_FOR_PICKUP,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum UserRole {
        CUSTOMER,
        DELIVERY_PERSON,
        ADMIN,
        RESTAURANT_OWNER
    }
    
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, UPI, CASH_ON_DELIVERY
    }
    
    public enum DiscountType {
        PERCENTAGE, FIXED
    }
}
