package com.quind.ecommerce.payment.application.service;

import com.quind.ecommerce.payment.domain.events.PaymentEvent;
import com.quind.ecommerce.payment.domain.model.Payment;
import com.quind.ecommerce.payment.domain.model.PaymentStatus;
import com.quind.ecommerce.payment.domain.ports.PaymentEventPublisherPort;
import com.quind.ecommerce.payment.domain.ports.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentEventPublisherPort paymentEventPublisher;

    /**
     * Paso 4 de la Saga:
     *  - Idempotencia: si ya existe un pago "finalizado OK" para ese orderId, lo devuelve sin reprocesar.
     *  - Si no existe o no está finalizado, simula el gateway, actualiza estado y emite
     *    PAYMENT_PROCESSED / PAYMENT_FAILED a Kafka.
     */
    public Mono<Payment> processPayment(UUID orderId, BigDecimal amount) {
        return paymentRepository.findByOrderId(orderId)
                // ✅ LAZY: solo ejecuta createNewPayment si el Mono está vacío
                .switchIfEmpty(Mono.defer(() -> createNewPayment(orderId, amount)))
                .flatMap(payment -> {
                    // Idempotencia: si ya está SUCCESS, no reprocesamos ni republicamos
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        return Mono.just(payment);
                    }

                    // --- Simulación de gateway de pago ---
                    boolean approved = amount != null && amount.compareTo(BigDecimal.ZERO) > 0;

                    if (approved) {
                        payment.setStatus(PaymentStatus.SUCCESS);  // o APPROVED / PAID según tu enum
                        payment.setFailureReason(null);
                    } else {
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setFailureReason("Payment declined by gateway");
                    }

                    payment.setUpdatedAt(Instant.now());

                    // Crear el PaymentEvent a partir de la info del pago
                    PaymentEvent event = approved
                            ? PaymentEvent.processed(
                                    orderId,
                                    amount,
                                    payment.getStatus().name(),
                                    "Payment processed successfully"
                            )
                            : PaymentEvent.failed(
                                    orderId,
                                    amount,
                                    payment.getStatus().name(),
                                    payment.getFailureReason()
                            );

                    // Guardar el pago y publicar el evento
                    return paymentRepository.save(payment)
                            .flatMap(saved ->
                                    paymentEventPublisher.publish(event)
                                            .thenReturn(saved)
                            );
                });
    }

    private Mono<Payment> createNewPayment(UUID orderId, BigDecimal amount) {
        Instant now = Instant.now();

        Payment payment = Payment.builder()
                .id(null)                             // el adapter genera el UUID
                .orderId(orderId)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .status(PaymentStatus.PROCESSING)     // estado inicial: PROCESSING
                .failureReason(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return paymentRepository.save(payment);
    }
}
