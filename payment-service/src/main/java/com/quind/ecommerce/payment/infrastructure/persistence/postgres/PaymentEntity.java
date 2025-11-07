package com.quind.ecommerce.payment.infrastructure.persistence.postgres;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    private BigDecimal amount;

    private String status;

    @Column("failure_reason")
    private String failureReason;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    /**
     * Flag para controlar si la entidad es nueva (INSERT) o existente (UPDATE).
     * No se persiste en la tabla.
     */
    @Transient
    private boolean isNew;

    /**
     * NO marcar este getter como @Transient.
     * Spring Data usa este método para detectar la propiedad identificadora.
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Indica a Spring Data si debe hacer INSERT (true) o UPDATE (false).
     */
    @Override
    @Transient
    public boolean isNew() {
        return isNew || id == null;
    }

    /**
     * Helper opcional para marcar la entidad como nueva al crearla.
     */
    public static PaymentEntity newEntity(UUID orderId,
                                          BigDecimal amount,
                                          String status,
                                          String failureReason,
                                          Instant createdAt,
                                          Instant updatedAt) {
        return PaymentEntity.builder()
                .id(null) // se generará al insertar
                .orderId(orderId)
                .amount(amount)
                .status(status)
                .failureReason(failureReason)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .isNew(true)
                .build();
    }
}
