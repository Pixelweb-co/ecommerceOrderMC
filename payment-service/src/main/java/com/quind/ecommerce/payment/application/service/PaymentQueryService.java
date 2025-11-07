package com.quind.ecommerce.payment.application.service;

import com.quind.ecommerce.payment.domain.model.Payment;
import com.quind.ecommerce.payment.domain.ports.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepositoryPort paymentRepository;

    public Mono<Payment> getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
