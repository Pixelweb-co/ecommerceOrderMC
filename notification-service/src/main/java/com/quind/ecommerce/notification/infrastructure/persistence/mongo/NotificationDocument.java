package com.quind.ecommerce.notification.infrastructure.persistence.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class NotificationDocument {

    @Id
    private String id;

    private String orderId;
    private String type;
    private String message;
    private Instant createdAt;
}
