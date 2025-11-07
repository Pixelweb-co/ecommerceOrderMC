package com.quind.ecommerce.order.domain.events;

public enum OrderEventType {
    ORDER_CREATED,
    ORDER_CANCELLED,
    ORDER_STATUS_CHANGED,
    ORDER_CONFIRMED,
    ORDER_FAILED
}
