package com.quind.ecommerce.payment.infrastructure.web;

import com.quind.ecommerce.payment.application.service.PaymentCommandService;
import com.quind.ecommerce.payment.application.service.PaymentQueryService;
import com.quind.ecommerce.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentQueryService queryService;
    private final PaymentCommandService commandService;

    @GetMapping("/{orderId}")
    public Mono<Payment> getPaymentByOrderId(@PathVariable("orderId") UUID orderId) {
        return queryService.getByOrderId(orderId);
    }

    /**
     * POST /api/v1/payments/{orderId}/retry
     *
     * Reintenta el procesamiento de pago para la orden dada.
     * - Si no existe Payment para esa orden → 404
     * - Si existe → llama a processPayment(orderId, amount)
     *   (el propio PaymentCommandService maneja idempotencia).
     */
    @PostMapping("/{orderId}/retry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Payment> retryPayment(@PathVariable("orderId") UUID orderId) {
        return queryService.getByOrderId(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found for order " + orderId
                )))
                .flatMap(existingPayment ->
                        commandService.processPayment(orderId, existingPayment.getAmount())
                );
    }
}
