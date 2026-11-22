package com.onlineshop.catalogue.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String name) {
        super("Category already exists: " + name);
    }
}