package com.quind.ecommerce.order.application.service;

import com.quind.ecommerce.order.application.command.CancelOrderCommand;
import com.quind.ecommerce.order.application.command.CreateOrderCommand;
import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.events.OrderEventType;
import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.domain.model.OrderStatus;
import com.quind.ecommerce.order.domain.ports.DomainEventPublisherPort;
import com.quind.ecommerce.order.domain.ports.OrderEventStorePort;
import com.quind.ecommerce.order.domain.ports.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventStorePort eventStore;
    private final DomainEventPublisherPort publisher;

    /**
     * Paso 1 + Paso 2 de la Saga:
     *  - Crea la orden PENDING y emite ORDER_CREATED
     *  - Simula ValidateInventory y emite ORDER_CONFIRMED u ORDER_FAILED
     */
    public Mono<Order> createOrder(CreateOrderCommand command) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .customerId(command.getCustomerId())
                .totalAmount(command.getTotalAmount() != null ? command.getTotalAmount() : BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Evento de creación de orden
        OrderEvent createdEvent = OrderEvent.builder()
                .id(UUID.randomUUID())
                .orderId(order.getId())
                .type(OrderEventType.ORDER_CREATED)
                .occurredAt(Instant.now())
                .payload(Map.of(
                        "customerId", order.getCustomerId(),
                        "totalAmount", order.getTotalAmount(),
                        "status", order.getStatus().name()
                ))
                .build();

        // Guardamos la orden, registramos ORDER_CREATED y luego ejecutamos ValidateInventory
        return orderRepository.save(order)
                .flatMap(savedOrder ->
                        eventStore.append(createdEvent)
                                .then(publisher.publish(createdEvent))
                                .then(validateInventoryAndConfirm(savedOrder))
                );
    }

    /**
     * Cancelar orden:
     *  - 404 si no existe
     *  - 409 si no está en PENDING o CONFIRMED
     *  - ORDER_CANCELLED si la cancelación es válida
     */
    public Mono<Order> cancelOrder(CancelOrderCommand command) {
        return orderRepository.findById(command.getOrderId())
                // Si no existe → 404
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                )))
                .flatMap(order -> {
                    // Solo se puede cancelar si está en PENDING o CONFIRMED
                    if (order.getStatus() != OrderStatus.PENDING
                            && order.getStatus() != OrderStatus.CONFIRMED) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Cannot cancel order in status " + order.getStatus()
                        ));
                    }

                    // Lógica de dominio
                    order.cancel();

                    OrderEvent event = OrderEvent.builder()
                            .id(UUID.randomUUID())
                            .orderId(order.getId())
                            .type(OrderEventType.ORDER_CANCELLED)
                            .occurredAt(Instant.now())
                            .payload(Map.of(
                                    "status", order.getStatus().name()
                            ))
                            .build();

                    return orderRepository.save(order)
                            .flatMap(saved ->
                                    eventStore.append(event)
                                            .then(publisher.publish(event))
                                            .thenReturn(saved)
                            );
                });
    }

    /**
     * Paso 2 de la Saga: ValidateInventory
     *  - Si hay inventario → CONFIRMED + ORDER_CONFIRMED
     *  - Si no → FAILED + ORDER_FAILED
     *
     * Por ahora la validación es simulada (puedes meter reglas según totalAmount, stock, etc.)
     */
    private Mono<Order> validateInventoryAndConfirm(Order order) {
        // 🔧 REGLA SIMULADA:
        // Por ahora asumimos que siempre hay inventario.
        boolean hasInventory = true;

        if (hasInventory) {
            // PENDING -> CONFIRMED
            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(Instant.now());

            OrderEvent confirmedEvent = OrderEvent.builder()
                    .id(UUID.randomUUID())
                    .orderId(order.getId())
                    .type(OrderEventType.ORDER_CONFIRMED)
                    .occurredAt(Instant.now())
                    .payload(Map.of(
                            "status", order.getStatus().name(),
                            "reason", "Inventory validated and reserved",
                            "totalAmount", order.getTotalAmount()
                    ))
                    .build();

            return orderRepository.save(order)
                    .flatMap(saved ->
                            eventStore.append(confirmedEvent)
                                    .then(publisher.publish(confirmedEvent))
                                    .thenReturn(saved)
                    );
        } else {
            // PENDING -> FAILED (por inventario)
            order.setStatus(OrderStatus.FAILED);
            order.setUpdatedAt(Instant.now());

            OrderEvent failedEvent = OrderEvent.builder()
                    .id(UUID.randomUUID())
                    .orderId(order.getId())
                    .type(OrderEventType.ORDER_FAILED)
                    .occurredAt(Instant.now())
                    .payload(Map.of(
                            "status", order.getStatus().name(),
                            "reason", "Inventory not available"
                    ))
                    .build();

            return orderRepository.save(order)
                    .flatMap(saved ->
                            eventStore.append(failedEvent)
                                    .then(publisher.publish(failedEvent))
                                    .thenReturn(saved)
                    );
        }
    }
}
