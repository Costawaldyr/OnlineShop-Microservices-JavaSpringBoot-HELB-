package com.onlineshop.catalogue.entityTest;

import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ProductEntityTest {
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
        // Create fresh test data before each test.
        category = new CategoryEntity(CATEGORY_NAME, DESCRIPTION);
        category.setId(CATEGORY_ID);
        product = new ProductEntity(PRODUCT_NAME, DESCRIPTION, PRICE, STOCK, category);
        product.setId(PRODUCT_ID);
    }

    @Test
    public void shouldInitializeConstructorFields() {
        // Assert the values assigned by the constructor.
        assertEquals(PRODUCT_NAME, product.getName());
        assertEquals(DESCRIPTION, product.getDescription());
        assertEquals(PRICE, product.getPrice());
        assertEquals(Integer.valueOf(STOCK), product.getStock());
        assertSame(category, product.getCategory());
    }

    @Test
    public void shouldCompareEntitiesWithSameId() {
        // Arrange
        ProductEntity other = new ProductEntity();
        other.setId(PRODUCT_ID);

        // Act and assert
        assertEquals(product, other);
        assertEquals(product.hashCode(), other.hashCode());
    }

    @Test
    public void shouldKeepNewEntitiesDistinct() {
        // Entities without an ID represent different new objects.
        assertNotEquals(new ProductEntity(), new ProductEntity());
    }
}
