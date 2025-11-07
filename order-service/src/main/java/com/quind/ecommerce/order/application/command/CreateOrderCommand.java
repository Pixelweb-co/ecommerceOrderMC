package com.quind.ecommerce.order.application.command;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CreateOrderCommand {
    String customerId;
    BigDecimal totalAmount;
}
