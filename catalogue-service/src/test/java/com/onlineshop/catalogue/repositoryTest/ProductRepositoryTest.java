package com.onlineshop.catalogue.repositoryTest;

import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.exception.ProductNotFoundException;
import com.onlineshop.catalogue.repository.CategoryRepository;
import com.onlineshop.catalogue.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    private static final String CATEGORY_NAME = "Computing";
    private static final String OTHER_CATEGORY_NAME = "Gaming";
    private static final String CATEGORY_DESCRIPTION = "Computer equipment";

    private static final String PRODUCT_NAME = "Keyboard";
    private static final String PRODUCT_DESCRIPTION = "USB keyboard";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("29.99");

    private static final int INITIAL_STOCK = 10;
    private static final int REQUESTED_QUANTITY = 2;
    private static final int EXCESSIVE_QUANTITY = INITIAL_STOCK + 1;
    private static final int EMPTY_STOCK = 0;

    private static final int ONE_UPDATED_ROW = 1;
    private static final int NO_UPDATED_ROWS = 0;

    private static final Long UNKNOWN_PRODUCT_ID = -1L;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductRepository products;

    @Autowired
    private EntityManager entityManager;

    private CategoryEntity category;
    private ProductEntity product;

    @BeforeEach
    public void setUp() {
        // Create a category before each test.
        CategoryEntity newCategory = new CategoryEntity(
                CATEGORY_NAME,
                CATEGORY_DESCRIPTION
        );

        category = categories.save(newCategory);

        // Create a product linked to the category.
        ProductEntity newProduct = new ProductEntity(
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                INITIAL_STOCK,
                category
        );

        product = products.save(newProduct);
    }

    @Test
    public void shouldReturnProductWhenIdExists() {
        // Arrange
        Long productId = product.getId();
        Long categoryId = category.getId();

        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<ProductEntity> foundProduct = products.findById(productId);

        // Assert
        assertTrue(foundProduct.isPresent());

        ProductEntity actualProduct = foundProduct.get();

        assertEquals(productId, actualProduct.getId());
        assertEquals(PRODUCT_NAME, actualProduct.getName());
        assertEquals(PRODUCT_DESCRIPTION, actualProduct.getDescription());
        assertEquals(PRODUCT_PRICE, actualProduct.getPrice());
        assertEquals(INITIAL_STOCK, actualProduct.getStock().intValue());
        assertEquals(categoryId, actualProduct.getCategory().getId());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenProductDoesNotExist() {
        // Act
        Optional<ProductEntity> foundProduct =
                products.findById(UNKNOWN_PRODUCT_ID);

        // Assert
        assertTrue(foundProduct.isEmpty());
    }

    @Test
    public void shouldReturnProductWhenRequiredIdExists() {
        // Act
        ProductEntity foundProduct =
                products.requireById(product.getId());

        // Assert
        assertEquals(product.getId(), foundProduct.getId());
        assertEquals(PRODUCT_NAME, foundProduct.getName());
    }

    @Test
    public void shouldThrowWhenRequiredProductDoesNotExist() {
        // Act and assert
        assertThrows(
                ProductNotFoundException.class,
                () -> products.requireById(UNKNOWN_PRODUCT_ID)
        );
    }

    @Test
    public void shouldFindProductsByCategory() {
        // Arrange
        CategoryEntity otherCategory = categories.save(
                new CategoryEntity(
                        OTHER_CATEGORY_NAME,
                        CATEGORY_DESCRIPTION
                )
        );

        ProductEntity otherProduct = new ProductEntity(
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                INITIAL_STOCK,
                otherCategory
        );

        products.save(otherProduct);

        // Act
        List<ProductEntity> foundProducts =
                products.findByCategoryId(category.getId());

        // Assert
        assertEquals(List.of(product), foundProducts);
    }

    @Test
    public void shouldDecreaseStockWhenQuantityIsAvailable() {
        // Arrange
        Long productId = product.getId();
        int expectedStock = INITIAL_STOCK - REQUESTED_QUANTITY;

        entityManager.flush();

        // Act
        int updatedRows =
                products.decreaseStock(productId, REQUESTED_QUANTITY);

        // Reload the product after the update query.
        entityManager.clear();

        ProductEntity updatedProduct = products.requireById(productId);

        // Assert
        assertEquals(ONE_UPDATED_ROW, updatedRows);
        assertEquals(expectedStock, updatedProduct.getStock().intValue());
    }

    @Test
    public void shouldKeepStockWhenQuantityIsInsufficient() {
        // Arrange
        Long productId = product.getId();

        entityManager.flush();

        // Act
        int updatedRows =
                products.decreaseStock(productId, EXCESSIVE_QUANTITY);

        entityManager.clear();

        ProductEntity unchangedProduct = products.requireById(productId);

        // Assert
        assertEquals(NO_UPDATED_ROWS, updatedRows);
        assertEquals(
                INITIAL_STOCK,
                unchangedProduct.getStock().intValue()
        );
    }

    @Test
    public void shouldDecreaseStockToZeroWhenBuyingAllUnits() {
        // Arrange
        Long productId = product.getId();

        entityManager.flush();

        // Act
        int updatedRows =
                products.decreaseStock(productId, INITIAL_STOCK);

        entityManager.clear();

        ProductEntity updatedProduct = products.requireById(productId);

        // Assert
        assertEquals(ONE_UPDATED_ROW, updatedRows);
        assertEquals(EMPTY_STOCK, updatedProduct.getStock().intValue());
    }

    @Test
    public void shouldNotUpdateStockWhenProductDoesNotExist() {
        // Act
        int updatedRows = products.decreaseStock(
                UNKNOWN_PRODUCT_ID,
                REQUESTED_QUANTITY
        );

        // Assert
        assertEquals(NO_UPDATED_ROWS, updatedRows);
    }

    @Test
    public void shouldDeleteProductAndKeepCategory() {
        // Arrange
        Long productId = product.getId();
        Long categoryId = category.getId();

        // Act
        products.delete(product);

        entityManager.flush();
        entityManager.clear();

        Optional<ProductEntity> deletedProduct =
                products.findById(productId);

        Optional<CategoryEntity> remainingCategory =
                categories.findById(categoryId);

        // Assert
        assertTrue(deletedProduct.isEmpty());
        assertTrue(remainingCategory.isPresent());
    }
}