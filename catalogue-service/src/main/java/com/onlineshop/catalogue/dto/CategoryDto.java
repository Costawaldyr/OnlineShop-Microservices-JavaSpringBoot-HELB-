package com.onlineshop.catalogue.dto;

import java.util.List;


public record CategoryDto(
        Long id,
        String name,
        String description,
        List<ProductDto> productList
) {
}
