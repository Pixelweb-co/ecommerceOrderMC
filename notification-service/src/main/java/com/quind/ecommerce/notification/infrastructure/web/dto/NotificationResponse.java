package com.quind.ecommerce.notification.infrastructure.web.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class NotificationResponse {

    UUID id;
    UUID orderId;
    String type;
    String message;
    Instant createdAt;
}
