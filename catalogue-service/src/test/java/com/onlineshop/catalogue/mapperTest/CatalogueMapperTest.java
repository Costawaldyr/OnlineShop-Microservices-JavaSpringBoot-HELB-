package com.onlineshop.catalogue.mapperTest;

import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.mapper.CatalogueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CatalogueMapperTest {
    private CatalogueMapper mapper;
    private static final Long CATEGORY_ID = 2L;
    private static final Long PRODUCT_ID = 1L;
    private static final String CATEGORY_NAME = "Computing";
    private static final String DESCRIPTION = "Computer equipment";
    private static final String PRODUCT_NAME = "Keyboard";
    private static final BigDecimal PRICE = new BigDecimal("29.99");
    private static final int STOCK = 10;
    private CategoryEntity category;
    private ProductEntity product;

    @BeforeEach
    public void setUp() {
        mapper = new CatalogueMapper();

        category = new CategoryEntity(CATEGORY_NAME, DESCRIPTION);
        category.setId(CATEGORY_ID);
        product = new ProductEntity(PRODUCT_NAME, DESCRIPTION, PRICE, STOCK, category);
        product.setId(PRODUCT_ID);
    }

    @Test
    public void shouldMapEveryProductField() {
        // Act
        ProductDto productDto = mapper.toProductDto(product);

        // Assert each mapped field.
        assertEquals(PRODUCT_ID, productDto.id());
        assertEquals(PRODUCT_NAME, productDto.name());
        assertEquals(DESCRIPTION, productDto.description());
        assertEquals(PRICE, productDto.price());
        assertEquals(STOCK, productDto.stock().intValue());
        assertEquals(CATEGORY_ID, productDto.categoryId());
    }

    @Test
    public void shouldMapCategoryWithProducts() {
        // Keep both sides of the in-memory relationship consistent.
        category.getProductList().add(product);

        // Act
        CategoryDto result = mapper.toCategoryDto(category);

        // Assert
        assertEquals(CATEGORY_ID, result.id());
        assertEquals(CATEGORY_NAME, result.name());
        assertEquals(DESCRIPTION, result.description());
        ProductDto expectedProduct = new ProductDto(
                PRODUCT_ID, PRODUCT_NAME, DESCRIPTION, PRICE, STOCK, CATEGORY_ID
        );
        assertEquals(List.of(expectedProduct), result.productList());
    }

    @Test
    public void shouldMapCategoryWithoutProducts() {
        // Act
        CategoryDto categoryDto = mapper.toCategoryDto(category);

        // Assert
        assertTrue(categoryDto.productList().isEmpty());
    }
}
