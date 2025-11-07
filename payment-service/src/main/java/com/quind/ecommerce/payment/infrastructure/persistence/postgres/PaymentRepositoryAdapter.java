package com.quind.ecommerce.payment.infrastructure.persistence.postgres;

import com.quind.ecommerce.payment.domain.model.Payment;
import com.quind.ecommerce.payment.domain.model.PaymentStatus;
import com.quind.ecommerce.payment.domain.ports.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentR2dbcRepository repository;

    @Override
    public Mono<Payment> save(Payment payment) {

        boolean isNew = (payment.getId() == null);

        UUID id = isNew ? UUID.randomUUID() : payment.getId();
        Instant now = Instant.now();

        // Si el dominio ya envía createdAt, lo respetamos; si no, usamos "now"
        Instant createdAt = payment.getCreatedAt() != null
                ? payment.getCreatedAt()
                : now;

        PaymentEntity entity = PaymentEntity.builder()
                .id(id)                                  // nunca va null
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .createdAt(createdAt)                   // se conserva si venía del dominio
                .updatedAt(now)                         // siempre refrescamos updatedAt
                .isNew(isNew)                           // le dice a Spring: INSERT si es nuevo, UPDATE si no
                .build();

        return repository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Payment> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .map(this::toDomain);
    }

    private Payment toDomain(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .amount(entity.getAmount())
                .status(PaymentStatus.valueOf(entity.getStatus()))
                .failureReason(entity.getFailureReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}