package com.quind.ecommerce.payment.infrastructure.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
