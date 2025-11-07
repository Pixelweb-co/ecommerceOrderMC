package com.quind.ecommerce.notification.application.service;

import com.quind.ecommerce.notification.domain.model.Notification;
import com.quind.ecommerce.notification.domain.ports.NotificationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort repository;

    @InjectMocks
    private NotificationService service;

    @Test
    void getByOrderId_shouldReturnNotificationsFromRepository() {
        UUID orderId = UUID.randomUUID();

        Notification n1 = Notification.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type("ORDER_CREATED")
                .message("Tu orden ha sido creada")
                .createdAt(Instant.now())
                .build();

        Notification n2 = Notification.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type("ORDER_CONFIRMED")
                .message("Tu orden ha sido confirmada")
                .createdAt(Instant.now())
                .build();

        when(repository.findByOrderId(orderId))
                .thenReturn(Flux.just(n1, n2));

        StepVerifier.create(service.getByOrderId(orderId))
                .expectNext(n1)
                .expectNext(n2)
                .verifyComplete();
    }

    @Test
    void createNotification_shouldReturnExistingWhenSameTypeAndMessage() {
        UUID orderId = UUID.randomUUID();

        Notification existing = Notification.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .type("ORDER_CONFIRMED")
                .message("Tu orden ha sido confirmada")
                .createdAt(Instant.now())
                .build();

        // El servicio filtra por type+message sobre todas las notificaciones del orderId
        when(repository.findByOrderId(orderId))
                .thenReturn(Flux.just(existing));

        StepVerifier.create(
                        service.createNotification(orderId,
                                "ORDER_CONFIRMED",
                                "Tu orden ha sido confirmada")
                )
                .assertNext(notification ->
                        assertEquals(existing.getId(), notification.getId()))
                .verifyComplete();

        // No se debe guardar nada nuevo
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldSaveNewWhenNotExists() {
        UUID orderId = UUID.randomUUID();
        String type = "ORDER_CREATED";
        String message = "Tu orden ha sido creada";

        when(repository.findByOrderId(orderId))
                .thenReturn(Flux.empty());

        // Guardamos lo que llegue y lo devolvemos
        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.createNotification(orderId, type, message))
                .assertNext(notification -> {
                    assertEquals(orderId, notification.getOrderId());
                    assertEquals(type, notification.getType());
                    assertEquals(message, notification.getMessage());
                })
                .verifyComplete();

        verify(repository).save(any(Notification.class));
    }
}
