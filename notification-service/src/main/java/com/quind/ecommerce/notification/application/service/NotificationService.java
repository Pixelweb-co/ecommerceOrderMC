package com.quind.ecommerce.notification.application.service;

import com.quind.ecommerce.notification.domain.model.Notification;
import com.quind.ecommerce.notification.domain.ports.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepositoryPort repository;

    public Flux<Notification> getByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId);
    }

    /**
     * Crea una notificación de forma idempotente.
     *
     * Regla:
     *  - Si ya existe una notificación con el mismo (orderId, type, message),
     *    NO se crea una nueva; se devuelve la existente.
     *  - Si no existe, se crea y se persiste.
     */
   
     public Mono<Notification> createNotification(UUID orderId, String type, String message) {
    // Idempotencia: si ya existe una con mismo orderId + type + message, la devolvemos
    return repository.findByOrderId(orderId)
            .filter(n -> type.equals(n.getType()) && message.equals(n.getMessage()))
            .next() // toma la primera coincidencia o vacío
            .switchIfEmpty(Mono.defer(() -> {
                Notification notification = Notification.builder()
                        .id(UUID.randomUUID())
                        .orderId(orderId)
                        .type(type)
                        .message(message)
                        .createdAt(Instant.now())
                        .build();

                return repository.save(notification);
            }));
}

}