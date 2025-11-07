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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private OrderEventStorePort eventStore;

    @Mock
    private DomainEventPublisherPort publisher;

    @InjectMocks
    private OrderCommandService service;

    @Test
    void createOrder_shouldCreatePendingAndConfirmInventory() {
        // given
        CreateOrderCommand cmd = CreateOrderCommand.builder()
                .customerId("88")
                .totalAmount(new BigDecimal("25000"))
                .build();

        // el servicio hace 2 saves (PENDING y luego CONFIRMED)
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // stubs usados en este test
        when(eventStore.append(any(OrderEvent.class))).thenReturn(Mono.empty());
        when(publisher.publish(any(OrderEvent.class))).thenReturn(Mono.empty());

        // when
        Mono<Order> result = service.createOrder(cmd);

        // then
        StepVerifier.create(result)
                .assertNext(order -> {
                    assertEquals("88", order.getCustomerId());
                    assertEquals(new BigDecimal("25000"), order.getTotalAmount());
                    assertEquals(OrderStatus.CONFIRMED, order.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, atLeast(2)).save(any(Order.class));
        verify(eventStore, atLeast(1)).append(any(OrderEvent.class));
        verify(publisher, atLeast(1)).publish(any(OrderEvent.class));
    }

    @Test
    void cancelOrder_shouldCancelWhenPendingOrConfirmed() {
        UUID id = UUID.randomUUID();
        Order existing = Order.builder()
                .id(id)
                .customerId("88")
                .totalAmount(new BigDecimal("1000"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(id)).thenReturn(Mono.just(existing));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // stubs usados aquí
        when(eventStore.append(any(OrderEvent.class))).thenReturn(Mono.empty());
        when(publisher.publish(any(OrderEvent.class))).thenReturn(Mono.empty());

        CancelOrderCommand cmd = CancelOrderCommand.builder()
                .orderId(id)
                .build();

        Mono<Order> result = service.cancelOrder(cmd);

        StepVerifier.create(result)
                .assertNext(order -> assertEquals(OrderStatus.CANCELLED, order.getStatus()))
                .verifyComplete();

        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventStore).append(eventCaptor.capture());
        OrderEvent evt = eventCaptor.getValue();
        assertThat(evt.getType()).isEqualTo(OrderEventType.ORDER_CANCELLED);
        assertThat(evt.getPayload().get("status")).isEqualTo("CANCELLED");
    }

    @Test
    void cancelOrder_shouldReturn404WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Mono.empty());

        CancelOrderCommand cmd = CancelOrderCommand.builder()
                .orderId(id)
                .build();

        StepVerifier.create(service.cancelOrder(cmd))
                .expectErrorMatches(ex ->
                        ex instanceof ResponseStatusException &&
                                ((ResponseStatusException) ex).getStatusCode().value() == 404)
                .verify();

        // aquí NO stubemos eventStore/publisher, y tampoco se deben invocar
        verify(eventStore, never()).append(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void cancelOrder_shouldReturn409WhenInvalidStatus() {
        UUID id = UUID.randomUUID();
        Order existing = Order.builder()
                .id(id)
                .customerId("88")
                .totalAmount(new BigDecimal("1000"))
                .status(OrderStatus.PAID)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(id)).thenReturn(Mono.just(existing));

        CancelOrderCommand cmd = CancelOrderCommand.builder()
                .orderId(id)
                .build();

        StepVerifier.create(service.cancelOrder(cmd))
                .expectErrorMatches(ex ->
                        ex instanceof ResponseStatusException &&
                                ((ResponseStatusException) ex).getStatusCode().value() == 409)
                .verify();

        verify(eventStore, never()).append(any());
        verify(publisher, never()).publish(any());
    }
}
