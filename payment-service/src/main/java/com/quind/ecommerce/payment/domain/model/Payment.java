package com.quind.ecommerce.payment.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Payment {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean canRetry() {
        return status == PaymentStatus.FAILED;
    }
}
