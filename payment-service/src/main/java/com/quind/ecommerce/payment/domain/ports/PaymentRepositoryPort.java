package com.quind.ecommerce.payment.domain.ports;

import com.quind.ecommerce.payment.domain.model.Payment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepositoryPort {

    Mono<Payment> save(Payment payment);

    Mono<Payment> findByOrderId(UUID orderId);
}
