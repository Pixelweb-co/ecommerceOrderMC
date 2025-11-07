package com.quind.ecommerce.order.infrastructure.web.dto;

import com.quind.ecommerce.order.domain.events.OrderEventType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class OrderEventResponse {
    UUID orderId;
    OrderEventType type;
    Instant occurredAt;
    Map<String, Object> payload;
}
