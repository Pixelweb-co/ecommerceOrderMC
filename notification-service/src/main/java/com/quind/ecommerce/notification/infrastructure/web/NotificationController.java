package com.quind.ecommerce.notification.infrastructure.web;

import com.quind.ecommerce.notification.application.service.NotificationService;
import com.quind.ecommerce.notification.infrastructure.web.dto.NotificationResponse;
import com.quind.ecommerce.notification.infrastructure.web.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{orderId}")
    public Flux<NotificationResponse> getByOrderId(@PathVariable("orderId") UUID orderId) {
        return notificationService.getByOrderId(orderId)
                .map(NotificationMapper::toResponse);
    }
}
