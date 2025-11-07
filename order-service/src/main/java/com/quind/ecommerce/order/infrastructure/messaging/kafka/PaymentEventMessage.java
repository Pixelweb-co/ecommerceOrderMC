package com.quind.ecommerce.order.infrastructure.messaging.kafka;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class PaymentEventMessage {

    private UUID id;
    private UUID orderId;
    private String type;               // PAYMENT_PROCESSED / PAYMENT_FAILED
    private Instant occurredAt;
    private Map<String, Object> payload;

    // Estos campos vienen también a nivel raíz (según el log del publisher)
    private String message;
    private String status;             // SUCCESS / FAILED / etc.
    private BigDecimal amount;

    // Helpers por si quieres leerlos siempre desde payload
    public BigDecimal getResolvedAmount() {
        if (amount != null) {
            return amount;
        }
        if (payload == null) return BigDecimal.ZERO;
        Object value = payload.get("amount");
        if (value instanceof BigDecimal big) return big;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) {
            try { return new BigDecimal(s); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
        }
        return BigDecimal.ZERO;
    }

    public String getResolvedStatus() {
        if (status != null) return status;
        if (payload == null) return null;
        Object value = payload.get("status");
        return value != null ? value.toString() : null;
    }

    public String getResolvedMessage() {
        if (message != null) return message;
        if (payload == null) return null;
        Object value = payload.get("message");
        return value != null ? value.toString() : null;
    }
}
