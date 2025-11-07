package com.quind.ecommerce.notification.infrastructure.web.mapper;

import com.quind.ecommerce.notification.domain.model.Notification;
import com.quind.ecommerce.notification.infrastructure.web.dto.NotificationResponse;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
