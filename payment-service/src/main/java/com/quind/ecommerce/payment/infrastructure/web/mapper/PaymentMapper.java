package com.quind.ecommerce.payment.infrastructure.web.mapper;

import com.quind.ecommerce.payment.domain.model.Payment;
import com.quind.ecommerce.payment.infrastructure.web.dto.PaymentResponse;

public class PaymentMapper {

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
