package com.onlineshop.order.exception;

public class OrderLineNotFoundException extends RuntimeException {

    public OrderLineNotFoundException(Long orderId, Long lineId) {
        super(
                "Order line " + lineId + " not found in order " + orderId
        );
    }
}