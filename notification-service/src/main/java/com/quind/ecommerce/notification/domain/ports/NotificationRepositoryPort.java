package com.quind.ecommerce.notification.domain.ports;

import com.quind.ecommerce.notification.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NotificationRepositoryPort {

    Mono<Notification> save(Notification notification);

    Flux<Notification> findByOrderId(UUID orderId);
}
