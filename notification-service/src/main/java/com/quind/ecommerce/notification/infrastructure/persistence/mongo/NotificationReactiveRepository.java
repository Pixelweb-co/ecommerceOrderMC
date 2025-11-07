package com.quind.ecommerce.notification.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface NotificationReactiveRepository
        extends ReactiveMongoRepository<NotificationDocument, String> {

    Flux<NotificationDocument> findByOrderIdOrderByCreatedAtDesc(String orderId);
}
