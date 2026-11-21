package com.onlineshop.order.exception;

public class ClientAlreadyExistsException extends RuntimeException {
    public ClientAlreadyExistsException(String email) {
        super("Client already exists with email: " + email);
    }
}