package com.quind.ecommerce.order.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.ports.DomainEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisherPort {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(OrderEvent event) {
        String key = event.getOrderId().toString();
        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        log.info("Publishing event to Kafka: topic={}, key={}, payload={}", TOPIC, key, payload);

        // kafkaTemplate.send(...) devuelve CompletableFuture<SendResult<...>>
        return Mono.fromFuture(kafkaTemplate.send(TOPIC, key, payload))
                .doOnSuccess(result -> {
                    if (result != null && result.getRecordMetadata() != null) {
                        log.debug("Event published to Kafka partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.debug("Event published to Kafka (metadata not available)");
                    }
                })
                .doOnError(ex -> log.error("Error publishing event to Kafka", ex))
                .then();
    }
}
