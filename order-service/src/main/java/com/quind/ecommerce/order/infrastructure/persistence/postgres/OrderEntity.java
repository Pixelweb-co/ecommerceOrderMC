package com.quind.ecommerce.order.infrastructure.persistence.postgres;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    private String customerId;

    private BigDecimal totalAmount;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    @Transient
    private Boolean isNew;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // true  -> INSERT
        // false -> UPDATE
        return Boolean.TRUE.equals(isNew);
    }
}
