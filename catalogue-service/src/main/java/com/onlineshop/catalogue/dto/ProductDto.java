package com.onlineshop.catalogue.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductDto (
         Long id,

         @NotBlank(message = "is required")
         String name,

         @NotBlank(message = "is required")
         String description,

         @NotNull(message = "is required")
         @Positive(message = "must be greater than 0")
         @Digits(integer = 8, fraction = 2, message = "must have at most 2 decimals")
         BigDecimal price,

         @NotNull(message = "is required")
         @PositiveOrZero(message = "must not be negative")
         Integer stock,

         @NotNull(message = "is required")
         Long categoryId
) {
}