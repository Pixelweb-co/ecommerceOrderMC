package com.quind.ecommerce.payment.application.service;

import com.quind.ecommerce.payment.domain.model.Payment;
import com.quind.ecommerce.payment.domain.model.PaymentStatus;
import com.quind.ecommerce.payment.domain.ports.PaymentEventPublisherPort;
import com.quind.ecommerce.payment.domain.ports.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {

    @Mock
    private PaymentRepositoryPort repository;

    @Mock
    private PaymentEventPublisherPort paymentEventPublisher;

    @InjectMocks
    private PaymentCommandService service;

    @Test
    void processPayment_shouldReturnExistingPayment_ifAlreadyExists() {
        // given
        UUID orderId = UUID.randomUUID();
        Payment existing = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(new BigDecimal("20000"))
                .status(PaymentStatus.SUCCESS)  // ya está finalizado OK
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Solo se usa findByOrderId en este escenario;
        // NO stubbeamos publish ni save para evitar UnnecessaryStubbing
        when(repository.findByOrderId(orderId)).thenReturn(Mono.just(existing));

        // when
        Mono<Payment> result = service.processPayment(orderId, new BigDecimal("20000"));

        // then
        StepVerifier.create(result)
                .assertNext(payment -> {
                    assertEquals(existing.getId(), payment.getId());
                    assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldCreateNew_whenNoExistingPayment() {
        // given
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("20000");

        when(repository.findByOrderId(orderId)).thenReturn(Mono.empty());

        // aquí sí se usa save(...)
        when(repository.save(any(Payment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // aquí sí se publica un evento, así que lo stubbeamos
        when(paymentEventPublisher.publish(any())).thenReturn(Mono.empty());

        // when
        Mono<Payment> result = service.processPayment(orderId, amount);

        // then
        StepVerifier.create(result)
                .assertNext(payment -> {
                    assertEquals(orderId, payment.getOrderId());
                    assertEquals(amount, payment.getAmount());
                    // ajusta según tu lógica real:
                    // assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
                    // o SUCCESS si lo marcas de una vez
                })
                .verifyComplete();
    }
}
