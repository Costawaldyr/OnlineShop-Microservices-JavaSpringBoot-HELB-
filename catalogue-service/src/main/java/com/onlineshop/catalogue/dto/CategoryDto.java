package com.onlineshop.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;


public record CategoryDto(

        Long id,

        @NotBlank(message = "is required")
        @Size(max = 100, message = "must have at most 100 characters")
        String name,

        String description,
        List<ProductDto> productList
) {
}
