package com.quind.ecommerce.order.infrastructure.web.mapper;

import com.quind.ecommerce.order.domain.events.OrderEvent;
import com.quind.ecommerce.order.domain.model.Order;
import com.quind.ecommerce.order.infrastructure.web.dto.OrderEventResponse;
import com.quind.ecommerce.order.infrastructure.web.dto.OrderResponse;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderEventResponse toResponse(OrderEvent event) {
        return OrderEventResponse.builder()
                .orderId(event.getOrderId())
                .type(event.getType())
                .occurredAt(event.getOccurredAt())
                .payload(event.getPayload())
                .build();
    }
}
