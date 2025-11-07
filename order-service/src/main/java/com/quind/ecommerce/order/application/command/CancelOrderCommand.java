package com.quind.ecommerce.order.application.command;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CancelOrderCommand {
    UUID orderId;
}
