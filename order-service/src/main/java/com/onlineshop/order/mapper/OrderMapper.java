package com.onlineshop.order.mapper;

import com.onlineshop.order.dto.ClientDto;
import com.onlineshop.order.dto.OrderDto;
import com.onlineshop.order.dto.OrderLineDto;
import com.onlineshop.order.entity.ClientEntity;
import com.onlineshop.order.entity.OrderEntity;
import com.onlineshop.order.entity.OrderLineEntity;
import org.springframework.stereotype.Component;


@Component
public class OrderMapper {

    public ClientDto toClientDto(ClientEntity client) {
        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getAddress()
        );
    }

    public OrderLineDto toOrderLineDto(OrderLineEntity orderLine) {
        return new OrderLineDto(
                orderLine.getId(),
                orderLine.getProductId(),
                orderLine.getQuantity(),
                orderLine.getUnitPrice()
        );
    }

    public OrderDto toOrderDto(OrderEntity order) {
        return new OrderDto(
                order.getId(),
                toClientDto(order.getClient()),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getOrderLineList()
                        .stream()
                        .map(this::toOrderLineDto)
                        .toList()
        );
    }
}
