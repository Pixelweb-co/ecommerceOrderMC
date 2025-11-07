package com.quind.ecommerce.order.infrastructure.web.dto;

import com.quind.ecommerce.order.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class OrderResponse {
    UUID id;
    String customerId;
    BigDecimal totalAmount;
    OrderStatus status;
    Instant createdAt;
    Instant updatedAt;
}
