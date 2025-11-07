package com.quind.ecommerce.payment.domain.ports;

import com.quind.ecommerce.payment.domain.events.PaymentEvent;
import reactor.core.publisher.Mono;

public interface PaymentEventPublisherPort {

    Mono<Void> publish(PaymentEvent event);
}
