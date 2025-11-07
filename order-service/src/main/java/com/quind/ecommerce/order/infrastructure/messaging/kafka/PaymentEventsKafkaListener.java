package com.quind.ecommerce.order.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.events.OrderEventType;
import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.domain.model.OrderStatus;
import com.quind.ecommerce.order.domain.ports.DomainEventPublisherPort;
import com.quind.ecommerce.order.domain.ports.OrderEventStorePort;
import com.quind.ecommerce.order.domain.ports.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventsKafkaListener {

    private final ObjectMapper objectMapper;
    private final OrderRepositoryPort orderRepository;
    private final OrderEventStorePort eventStore;
    private final DomainEventPublisherPort publisher;

    /**
     * Escucha payment-events como String JSON, lo convierte a PaymentEventMessage
     * y actualiza el estado de la orden (PAID / FAILED).
     */
    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-payment-events"
    )
    public void onPaymentEvent(String message) {
        log.info("OrderService received payment event: value={}", message);

        PaymentEventMessage event;
        try {
            event = objectMapper.readValue(message, PaymentEventMessage.class);
        } catch (Exception e) {
            log.error("Failed to deserialize payment event from Kafka. Raw payload={}", message, e);
            return;
        }

        UUID orderId = event.getOrderId();

        orderRepository.findById(orderId)
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.warn("Order not found for payment event, orderId={}", orderId)))
                .flatMap(order -> handlePaymentEvent(order, event))
                .subscribe();
    }

    private Mono<Order> handlePaymentEvent(Order order, PaymentEventMessage event) {
        // Idempotencia: si ya está PAID o FAILED, no hacemos nada
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FAILED) {
            log.info("Ignoring payment event for order {} because status is already {}",
                    order.getId(), order.getStatus());
            return Mono.just(order);
        }

        OrderStatus previousStatus = order.getStatus();

        String eventType = event.getType();
        String paymentMessage = event.getResolvedMessage();
        String paymentStatus = event.getResolvedStatus();

        if ("PAYMENT_PROCESSED".equals(eventType)) {
            order.setStatus(OrderStatus.PAID);   // o order.markPaid() si tienes método de dominio
        } else if ("PAYMENT_FAILED".equals(eventType)) {
            order.setStatus(OrderStatus.FAILED); // o order.markFailed(paymentMessage)
        } else {
            log.warn("Unknown payment event type={} for orderId={}", eventType, order.getId());
            return Mono.just(order);
        }

        order.setUpdatedAt(Instant.now());

        OrderEvent statusChangedEvent = OrderEvent.builder()
                .id(UUID.randomUUID())
                .orderId(order.getId())
                .type(OrderEventType.ORDER_STATUS_CHANGED)
                .occurredAt(Instant.now())
                .payload(Map.of(
                        "previousStatus", previousStatus.name(),
                        "newStatus", order.getStatus().name(),
                        "paymentStatus", paymentStatus,
                        "paymentMessage", paymentMessage
                ))
                .build();

        return orderRepository.save(order)
                .flatMap(saved ->
                        eventStore.append(statusChangedEvent)
                                .then(publisher.publish(statusChangedEvent))
                                .thenReturn(saved)
                );
    }
}
