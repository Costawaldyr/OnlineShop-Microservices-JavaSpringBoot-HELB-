package com.onlineshop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderDto(
        @NotNull
        @Positive
        Long clientId,

        @NotEmpty
        List<@NotNull @Valid CreateOrderLineDto> lines
) {
}
