package com.quind.ecommerce.payment.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quind.ecommerce.payment.application.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsKafkaListener {

    private final ObjectMapper objectMapper;
    private final PaymentCommandService paymentCommandService;

    @KafkaListener(
            topics = "order-events",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(@Payload String message, ConsumerRecord<String, String> record) {
        try {
            log.info("PaymentService received order event: key={}, value={}", record.key(), message);

            Map<?, ?> raw = objectMapper.readValue(message, Map.class);

            String type = (String) raw.get("type");
            if (!"ORDER_CREATED".equals(type)) {
                return;
            }

            String orderIdStr = (String) raw.get("orderId");
            UUID orderId = UUID.fromString(orderIdStr);

            Map<?, ?> payload = (Map<?, ?>) raw.get("payload");
            Number totalAmountNumber = (Number) payload.get("totalAmount");
            BigDecimal totalAmount = BigDecimal.valueOf(totalAmountNumber.doubleValue());

            paymentCommandService.processPayment(orderId, totalAmount)
                    .doOnSuccess(p ->
                            log.info("Payment stored for order {} amount {}", orderId, totalAmount))
                    .doOnError(ex ->
                            log.error("Error processing payment for order {}", orderId, ex))
                    .subscribe();

        } catch (Exception ex) {
            log.error("Error deserializing or processing order event message", ex);
        }
    }
}
