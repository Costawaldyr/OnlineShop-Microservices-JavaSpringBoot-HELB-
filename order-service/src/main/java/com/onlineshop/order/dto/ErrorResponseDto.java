package com.onlineshop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        @Schema(example = "404")
        int status,

        @Schema(example = "Client not found: 99")
        String message,

        @Schema(example = "2026-11-21T20:00:00")
        LocalDateTime timestamp
) {
}