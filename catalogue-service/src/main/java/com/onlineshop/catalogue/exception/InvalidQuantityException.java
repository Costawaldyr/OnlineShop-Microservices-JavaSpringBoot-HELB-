package com.onlineshop.catalogue.exception;

public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException(int quantity) {
        super("Quantity must be at least one, received: " + quantity);
    }
}