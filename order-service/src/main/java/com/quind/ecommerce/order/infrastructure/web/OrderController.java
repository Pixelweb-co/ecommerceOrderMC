package com.quind.ecommerce.order.infrastructure.web;

import com.quind.ecommerce.order.application.command.CancelOrderCommand;
import com.quind.ecommerce.order.application.command.CreateOrderCommand;
import com.quind.ecommerce.order.application.service.OrderCommandService;
import com.quind.ecommerce.order.application.service.OrderQueryService;
import com.quind.ecommerce.order.infrastructure.web.dto.CreateOrderRequest;
import com.quind.ecommerce.order.infrastructure.web.dto.OrderEventResponse;
import com.quind.ecommerce.order.infrastructure.web.dto.OrderResponse;
import com.quind.ecommerce.order.infrastructure.web.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva orden")
    public Mono<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .build();

        return commandService.createOrder(command)
                .map(OrderMapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public Mono<OrderResponse> getOrderById(@PathVariable(name = "id") UUID id) {
        return queryService.getOrderById(id)
                .map(OrderMapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "Listar órdenes del cliente")
    public Flux<OrderResponse> getOrdersByCustomer(
            @RequestParam(name = "customerId") String customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return queryService.getOrdersByCustomer(customerId, page, size)
                .map(OrderMapper::toResponse);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar orden PENDING o CONFIRMED")
    public Mono<OrderResponse> cancelOrder(@PathVariable(name = "id") UUID id) {
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(id)
                .build();

        return commandService.cancelOrder(command)
                .map(OrderMapper::toResponse);
    }

    @GetMapping("/{id}/events")
    @Operation(summary = "Historial de eventos de la orden (Event Sourcing)")
    public Flux<OrderEventResponse> getOrderEvents(@PathVariable(name = "id") UUID id) {
        return queryService.getOrderEvents(id)
                .map(OrderMapper::toResponse);
    }
}
