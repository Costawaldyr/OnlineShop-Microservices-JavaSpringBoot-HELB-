package com.onlineshop.catalogue.exception;

public class CategoryNotEmptyException extends RuntimeException {

    public CategoryNotEmptyException(Long categoryId) {
        super("Cannot delete a category with products: " + categoryId);
    }
}
