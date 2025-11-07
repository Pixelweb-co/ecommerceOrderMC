package com.quind.ecommerce.order.infrastructure.persistence.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "order_events")
public class OrderEventDocument {

    @Id
    private String id;

    private UUID orderId;
    private String type;
    private Instant occurredAt;
    private Map<String, Object> payload;
}
