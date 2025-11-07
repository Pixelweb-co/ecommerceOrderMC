package com.quind.ecommerce.order.domain.ports;

import com.quind.ecommerce.order.domain.events.OrderEvent;
import reactor.core.publisher.Mono;

public interface DomainEventPublisherPort {

    Mono<Void> publish(OrderEvent event);
}
