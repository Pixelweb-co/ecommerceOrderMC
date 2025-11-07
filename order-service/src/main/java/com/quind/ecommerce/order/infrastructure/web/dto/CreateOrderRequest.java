package com.quind.ecommerce.order.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;
}
