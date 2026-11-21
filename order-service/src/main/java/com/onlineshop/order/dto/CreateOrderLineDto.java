package com.onlineshop.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderLineDto(

        @NotNull
        @Positive
        Long productId,

        @Positive
        int quantity
) {
}
