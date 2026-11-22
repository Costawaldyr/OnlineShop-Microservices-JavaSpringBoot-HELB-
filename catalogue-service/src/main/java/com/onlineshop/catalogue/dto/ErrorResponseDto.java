package com.onlineshop.catalogue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        @Schema(example = "404")
        int status,

        @Schema(example = "Product not found: 99")
        String message,

        @Schema(example = "2026-11-21T14:30:00")
        LocalDateTime timestamp
) {
}