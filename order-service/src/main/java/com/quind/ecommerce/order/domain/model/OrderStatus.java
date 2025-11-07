package com.quind.ecommerce.order.domain.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PROCESSING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}
