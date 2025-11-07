package com.quind.ecommerce.payment.infrastructure.messaging.kafka;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class OrderEventMessage {

    private UUID id;
    private UUID orderId;
    private String type;
    private Instant occurredAt;
    private Map<String, Object> payload;

    /**
     * Helper para obtener el totalAmount desde el payload del evento.
     * El OrderService envía en el payload:
     *  "totalAmount": order.getTotalAmount()
     */
    public BigDecimal getTotalAmount() {
        if (payload == null) {
            return BigDecimal.ZERO;
        }

        Object value = payload.get("totalAmount");
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            // Puede venir como Double, Integer, Long, etc.
            return BigDecimal.valueOf(number.doubleValue());
        }

        if (value instanceof String stringValue) {
            try {
                return new BigDecimal(stringValue);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        return BigDecimal.ZERO;
    }
}
