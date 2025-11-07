package com.quind.ecommerce.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quind.ecommerce.notification.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        String value = record.value();
        log.info("NotificationService - Received order event: {}", value);
        try {
            OrderEventMessage event = objectMapper.readValue(value, OrderEventMessage.class);

            String message = switch (event.getType()) {
                case "ORDER_CREATED"   -> "Tu orden ha sido creada.";
                case "ORDER_CONFIRMED" -> "Tu orden ha sido confirmada.";
                case "ORDER_CANCELLED" -> "Tu orden ha sido cancelada.";
                default                -> "Actualización de orden: " + event.getType();
            };

            Mono<Void> result = notificationService
                    .createNotification(event.getOrderId(), event.getType(), message)
                    .doOnSuccess(n -> log.info("Notification stored for order {} type {}", event.getOrderId(), event.getType()))
                    .doOnError(ex -> log.error("Error storing notification", ex))
                    .then();

            result.subscribe(); // disparar reactivo (simple para este caso)
        } catch (Exception e) {
            log.error("Error processing order event message", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void onPaymentEvent(ConsumerRecord<String, String> record) {
        String value = record.value();
        log.info("NotificationService - Received payment event: {}", value);
        try {
            PaymentEventMessage event = objectMapper.readValue(value, PaymentEventMessage.class);

            String message = switch (event.getType()) {
                case "PAYMENT_SUCCESS" -> "Tu pago ha sido procesado exitosamente.";
                case "PAYMENT_FAILED"  -> "Tu pago ha fallado. Por favor revisa tu método de pago.";
                default                -> "Actualización de pago: " + event.getType();
            };

            Mono<Void> result = notificationService
                    .createNotification(event.getOrderId(), event.getType(), message)
                    .doOnSuccess(n -> log.info("Notification stored for order {} type {}", event.getOrderId(), event.getType()))
                    .doOnError(ex -> log.error("Error storing notification", ex))
                    .then();

            result.subscribe();
        } catch (Exception e) {
            log.error("Error processing payment event message", e);
        }
    }
}
