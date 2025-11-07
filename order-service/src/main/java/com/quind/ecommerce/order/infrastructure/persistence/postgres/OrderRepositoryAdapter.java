package com.quind.ecommerce.order.infrastructure.persistence.postgres;

import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.domain.model.OrderStatus;
import com.quind.ecommerce.order.domain.ports.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderR2dbcRepository repository;

    @Override
    public Mono<Order> save(Order order) {

        return repository.existsById(order.getId())
                .flatMap(exists -> {
                    OrderEntity entity = OrderEntity.builder()
                            .id(order.getId())
                            .customerId(order.getCustomerId())
                            .totalAmount(order.getTotalAmount())
                            .status(order.getStatus().name())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .isNew(!exists)     // 👈 si no existe => INSERT, si existe => UPDATE
                            .build();

                    return repository.save(entity).map(this::toDomain);
                });
    }

    @Override
    public Mono<Order> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Order> findByCustomerId(String customerId, int page, int size) {
        int skip = page * size;
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .skip(skip)
                .take(size)
                .map(this::toDomain);
    }

    private Order toDomain(OrderEntity entity) {
        return Order.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .totalAmount(entity.getTotalAmount())
                .status(OrderStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
