package com.onlineshop.order.exception;

public class CatalogueUnavailableException extends RuntimeException {

    public CatalogueUnavailableException(Throwable cause) {
        super("Catalogue service is unavailable", cause);
    }

    public CatalogueUnavailableException(String message) {
        super(message);
    }
}