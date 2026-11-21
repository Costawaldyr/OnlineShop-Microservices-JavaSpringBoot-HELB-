package com.onlineshop.order.dto;

import com.onlineshop.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        ClientDto client,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderLineDto> orderLineList
){
}
