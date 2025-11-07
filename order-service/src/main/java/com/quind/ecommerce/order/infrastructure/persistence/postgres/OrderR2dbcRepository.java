package com.quind.ecommerce.order.infrastructure.persistence.postgres;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderR2dbcRepository extends R2dbcRepository<OrderEntity, UUID> {

    Flux<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
