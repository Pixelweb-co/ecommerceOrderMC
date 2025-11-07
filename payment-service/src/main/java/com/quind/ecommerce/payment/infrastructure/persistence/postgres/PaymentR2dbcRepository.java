package com.quind.ecommerce.payment.infrastructure.persistence.postgres;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentR2dbcRepository extends R2dbcRepository<PaymentEntity, UUID> {

    Mono<PaymentEntity> findByOrderId(UUID orderId);
}
