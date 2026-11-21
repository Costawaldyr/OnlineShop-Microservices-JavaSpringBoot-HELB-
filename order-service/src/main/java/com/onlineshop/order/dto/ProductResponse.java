package com.onlineshop.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock
) {
}