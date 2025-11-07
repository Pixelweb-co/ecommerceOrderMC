package com.quind.ecommerce.order.domain.ports;

import com.quind.ecommerce.order.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepositoryPort {

    Mono<Order> save(Order order);

    Mono<Order> findById(UUID id);

    Flux<Order> findByCustomerId(String customerId, int page, int size);
}
