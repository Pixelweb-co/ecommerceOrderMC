package com.quind.ecommerce.order.infrastructure.persistence.mongo;

import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.events.OrderEventType;
import com.quind.ecommerce.order.domain.ports.OrderEventStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderEventStoreAdapter implements OrderEventStorePort {

    private final OrderEventReactiveRepository repository;

    @Override
    public Mono<Void> append(OrderEvent event) {
        OrderEventDocument doc = OrderEventDocument.builder()
                .orderId(event.getOrderId())
                .type(event.getType().name())
                .occurredAt(event.getOccurredAt())
                .payload(event.getPayload())
                .build();

        return repository.save(doc).then();
    }

    @Override
    public Flux<OrderEvent> findByOrderId(java.util.UUID orderId) {
        return repository.findByOrderIdOrderByOccurredAtAsc(orderId)
                .map(doc -> OrderEvent.builder()
                        .id(java.util.UUID.randomUUID()) // solo para trazabilidad local
                        .orderId(doc.getOrderId())
                        .type(OrderEventType.valueOf(doc.getType()))
                        .occurredAt(doc.getOccurredAt())
                        .payload(doc.getPayload())
                        .build());
    }
}
