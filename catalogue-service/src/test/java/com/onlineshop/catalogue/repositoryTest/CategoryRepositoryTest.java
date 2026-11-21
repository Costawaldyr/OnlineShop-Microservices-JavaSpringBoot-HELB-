package com.onlineshop.catalogue.repositoryTest;

import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.exception.CategoryNotFoundException;
import com.onlineshop.catalogue.repository.CategoryRepository;
import com.onlineshop.catalogue.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest()
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {

    private static final String CATEGORY_NAME = "Computing";
    private static final String CATEGORY_NAME_UPPERCASE = "COMPUTING";
    private static final String CATEGORY_DESCRIPTION = "Computer equipment";

    private static final String PRODUCT_NAME = "Keyboard";
    private static final String PRODUCT_DESCRIPTION = "USB keyboard";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("29.99");
    private static final int INITIAL_STOCK = 10;

    private static final Long UNKNOWN_CATEGORY_ID = -1L;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductRepository products;

    @Autowired
    private EntityManager entityManager;

    private CategoryEntity category;

    @BeforeEach
    public void setUp() {
        // Create a category before each test.
        CategoryEntity newCategory = new CategoryEntity(
                CATEGORY_NAME,
                CATEGORY_DESCRIPTION
        );

        category = categories.save(newCategory);
    }

    @Test
    public void shouldReturnCategoryWhenIdExists() {
        // Arrange
        Long categoryId = category.getId();

        // Send pending changes to the database, then clear cached entities.
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<CategoryEntity> foundCategory =
                categories.findById(categoryId);

        // Assert
        assertTrue(foundCategory.isPresent());

        CategoryEntity actualCategory = foundCategory.get();

        assertEquals(categoryId, actualCategory.getId());
        assertEquals(CATEGORY_NAME, actualCategory.getName());
        assertEquals(
                CATEGORY_DESCRIPTION,
                actualCategory.getDescription()
        );
    }

    @Test
    public void shouldReturnEmptyOptionalWhenCategoryDoesNotExist() {
        // Act
        Optional<CategoryEntity> foundCategory =
                categories.findById(UNKNOWN_CATEGORY_ID);

        // Assert
        assertTrue(foundCategory.isEmpty());
    }

    @Test
    public void shouldFindCategoryNameIgnoringCase() {
        // Act
        boolean categoryExists =
                categories.existsByNameIgnoreCase(CATEGORY_NAME_UPPERCASE);

        // Assert
        assertTrue(categoryExists);
    }

    @Test
    public void shouldReturnCategoryWhenRequiredIdExists() {
        // Act
        CategoryEntity foundCategory =
                categories.requireById(category.getId());

        // Assert
        assertEquals(category.getId(), foundCategory.getId());
        assertEquals(CATEGORY_NAME, foundCategory.getName());
    }

    @Test
    public void shouldThrowWhenRequiredCategoryDoesNotExist() {
        // Act and assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> categories.requireById(UNKNOWN_CATEGORY_ID)
        );
    }


    @Test
    public void shouldLoadProductsRelatedToCategory() {
        // Arrange
        ProductEntity product = new ProductEntity(
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                INITIAL_STOCK,
                category
        );

        ProductEntity savedProduct = products.save(product);

        Long categoryId = category.getId();
        Long productId = savedProduct.getId();

        // Reload the relationship from the database.
        entityManager.flush();
        entityManager.clear();

        // Act
        CategoryEntity foundCategory = categories.requireById(categoryId);
        List<ProductEntity> foundProducts = foundCategory.getProductList();

        // Assert
        assertFalse(foundProducts.isEmpty());
        assertEquals(List.of(productId), foundProducts.stream()
                .map(ProductEntity::getId)
                .toList());
    }

    @Test
    public void shouldRejectDuplicateCategoryName() {
        // Arrange
        CategoryEntity duplicateCategory = new CategoryEntity(
                CATEGORY_NAME,
                CATEGORY_DESCRIPTION
        );

        // Act and assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    categories.save(duplicateCategory);

                    // Force PostgreSQL to check the unique constraint.
                    categories.flush();
                }
        );
    }

    @Test
    public void shouldDeleteEmptyCategory() {
        // Arrange
        Long categoryId = category.getId();

        // Act
        categories.delete(category);

        entityManager.flush();
        entityManager.clear();

        Optional<CategoryEntity> foundCategory =
                categories.findById(categoryId);

        // Assert
        assertTrue(foundCategory.isEmpty());
    }
}