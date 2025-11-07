package com.quind.ecommerce.order.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderEventReactiveRepository extends ReactiveMongoRepository<OrderEventDocument, String> {

    Flux<OrderEventDocument> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
