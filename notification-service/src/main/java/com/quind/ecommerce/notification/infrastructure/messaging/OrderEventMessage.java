package com.quind.ecommerce.notification.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEventMessage {

    private String type;
    private UUID orderId;

    // Lo dejamos como String para evitar problemas con Instant
    private String occurredAt;

    private Map<String, Object> payload;
}
