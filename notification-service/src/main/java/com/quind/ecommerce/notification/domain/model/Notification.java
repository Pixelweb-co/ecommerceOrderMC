package com.quind.ecommerce.notification.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class Notification {

    UUID id;
    UUID orderId;
    String type;        // ORDER_CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED, etc.
    String message;
    Instant createdAt;
}
