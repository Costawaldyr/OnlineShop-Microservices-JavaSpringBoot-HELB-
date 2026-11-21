package com.onlineshop.order.dto;


import java.math.BigDecimal;

public record OrderLineDto(
        Long id,
        Long productId,
        int quantity,
        BigDecimal unitPrice
) {
}