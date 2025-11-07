package com.quind.ecommerce.order.application.service;

import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.domain.ports.OrderEventStorePort;
import com.quind.ecommerce.order.domain.ports.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventStorePort eventStore;

    public Mono<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    public Flux<Order> getOrdersByCustomer(String customerId, int page, int size) {
        return orderRepository.findByCustomerId(customerId, page, size);
    }

    public Flux<OrderEvent> getOrderEvents(UUID orderId) {
        return eventStore.findByOrderId(orderId);
    }
}
