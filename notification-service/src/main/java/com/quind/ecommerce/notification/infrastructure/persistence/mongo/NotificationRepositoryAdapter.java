package com.quind.ecommerce.notification.infrastructure.persistence.mongo;

import com.quind.ecommerce.notification.domain.model.Notification;
import com.quind.ecommerce.notification.domain.ports.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationReactiveRepository repository;

    @Override
    public Mono<Notification> save(Notification notification) {

        // Dominio usa UUID, documento usa String
        NotificationDocument doc = NotificationDocument.builder()
                // usamos el UUID del dominio como _id en Mongo (en texto)
                .id(notification.getId() != null ? notification.getId().toString() : null)
                .orderId(notification.getOrderId().toString())  // 👈 se guarda como String
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .build();

        return repository.save(doc)
                .map(this::toDomain);
    }

    @Override
    public Flux<Notification> findByOrderId(UUID orderId) {
        // el repo de Mongo busca por String
        return repository.findByOrderIdOrderByCreatedAtDesc(orderId.toString())
                .map(this::toDomain);
    }

    private Notification toDomain(NotificationDocument doc) {
        UUID id = null;
        if (doc.getId() != null) {
            try {
                id = UUID.fromString(doc.getId());
            } catch (IllegalArgumentException ignored) {
                // si algún día hay documentos viejos con otro formato de _id, dejamos id en null
            }
        }

        return Notification.builder()
                .id(id)
                .orderId(UUID.fromString(doc.getOrderId()))
                .type(doc.getType())
                .message(doc.getMessage())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
