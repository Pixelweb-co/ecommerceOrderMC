package com.quind.ecommerce.payment.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quind.ecommerce.payment.domain.events.PaymentEvent;
import com.quind.ecommerce.payment.domain.ports.PaymentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaEventPublisher implements PaymentEventPublisherPort {

    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(PaymentEvent event) {
        String key = event.getOrderId().toString();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        log.info("Publishing PaymentEvent to Kafka: topic={}, key={}, payload={}", TOPIC, key, payload);

        return Mono.fromFuture(kafkaTemplate.send(TOPIC, key, payload))
                .doOnSuccess(result -> {
                    if (result != null && result.getRecordMetadata() != null) {
                        log.debug("PaymentEvent published partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                })
                .doOnError(ex -> log.error("Error publishing PaymentEvent to Kafka", ex))
                .then();
    }
}
