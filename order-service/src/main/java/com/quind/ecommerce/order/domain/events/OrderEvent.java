package com.quind.ecommerce.order.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class OrderEvent {

    private UUID id;
    private UUID orderId;
    private OrderEventType type;
    private Instant occurredAt;
    private Map<String, Object> payload;
}
