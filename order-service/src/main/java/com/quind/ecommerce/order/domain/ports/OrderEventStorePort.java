package com.quind.ecommerce.order.domain.ports;

import com.quind.ecommerce.order.domain.events.OrderEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderEventStorePort {

    Mono<Void> append(OrderEvent event);

    Flux<OrderEvent> findByOrderId(UUID orderId);
}
