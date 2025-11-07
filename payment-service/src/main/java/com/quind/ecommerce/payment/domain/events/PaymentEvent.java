package com.quind.ecommerce.payment.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class PaymentEvent {

    private UUID id;
    private UUID orderId;
    private PaymentEventType type;
    private Instant occurredAt;
    private Map<String, Object> payload;

    // --- Claves estándar en el payload ---
    public static final String KEY_AMOUNT  = "amount";
    public static final String KEY_STATUS  = "status";
    public static final String KEY_MESSAGE = "message";

    /**
     * Helper para obtener el monto del payload.
     */
    public BigDecimal getAmount() {
        if (payload == null) {
            return BigDecimal.ZERO;
        }

        Object value = payload.get(KEY_AMOUNT);
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal big) {
            return big;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (value instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Helper para obtener el status (APPROVED / DECLINED / ERROR...) del payload.
     */
    public String getStatus() {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(KEY_STATUS);
        return value != null ? value.toString() : null;
    }

    /**
     * Helper para obtener el mensaje del payload (detalle del gateway, error, etc.).
     */
    public String getMessage() {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(KEY_MESSAGE);
        return value != null ? value.toString() : null;
    }

    // --- Factory methods para crear eventos listos para la Saga ---

    public static PaymentEvent processed(UUID orderId,
                                         BigDecimal amount,
                                         String status,
                                         String message) {

        return PaymentEvent.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type(PaymentEventType.PAYMENT_PROCESSED)
                .occurredAt(Instant.now())
                .payload(Map.of(
                        KEY_AMOUNT, amount,
                        KEY_STATUS, status,
                        KEY_MESSAGE, message
                ))
                .build();
    }

    public static PaymentEvent failed(UUID orderId,
                                      BigDecimal amount,
                                      String status,
                                      String message) {

        return PaymentEvent.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type(PaymentEventType.PAYMENT_FAILED)
                .occurredAt(Instant.now())
                .payload(Map.of(
                        KEY_AMOUNT, amount,
                        KEY_STATUS, status,
                        KEY_MESSAGE, message
                ))
                .build();
    }
}
