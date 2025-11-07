package com.quind.ecommerce.order.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Order {

    private UUID id;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public void markPending() {
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be CONFIRMED from PENDING status");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void startPaymentProcessing() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order can only go to PAYMENT_PROCESSING from CONFIRMED status");
        }
        this.status = OrderStatus.PAYMENT_PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markPaid() {
        if (status != OrderStatus.PAYMENT_PROCESSING) {
            throw new IllegalStateException("Order can only be PAID from PAYMENT_PROCESSING status");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only PENDING or CONFIRMED orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
}
