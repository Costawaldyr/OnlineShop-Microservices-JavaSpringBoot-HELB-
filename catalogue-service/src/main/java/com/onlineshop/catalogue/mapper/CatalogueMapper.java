package com.onlineshop.catalogue.mapper;

import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class CatalogueMapper {

    public ProductDto toProductDto(ProductEntity product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getId()
        );
    }

    public CategoryDto toCategoryDto(CategoryEntity category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getProductList()
                        .stream()
                        .map(this::toProductDto)
                        .toList()
        );
    }
}