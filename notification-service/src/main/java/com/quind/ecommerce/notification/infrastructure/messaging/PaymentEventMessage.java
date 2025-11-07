package com.quind.ecommerce.notification.infrastructure.messaging;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class PaymentEventMessage {

    private String type;          // PAYMENT_SUCCESS, PAYMENT_FAILED
    private UUID orderId;
    private Instant occurredAt;
    private Map<String, Object> payload;
}
