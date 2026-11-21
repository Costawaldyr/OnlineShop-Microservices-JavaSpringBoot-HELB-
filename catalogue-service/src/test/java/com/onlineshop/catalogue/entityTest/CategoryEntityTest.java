package com.onlineshop.catalogue.entityTest;

import com.onlineshop.catalogue.entity.CategoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryEntityTest {

    private static final Long CATEGORY_ID = 2L;
    private static final String CATEGORY_NAME = "Computing";
    private static final String DESCRIPTION = "Computer equipment";
    private CategoryEntity category;

    @BeforeEach
    public void setUp() {
        // Create fresh test data before each test.
        category = new CategoryEntity(CATEGORY_NAME, DESCRIPTION);
        category.setId(CATEGORY_ID);
    }

    @Test
    public void shouldInitializeConstructorFields() {
        // Assert the values assigned by the constructor.
        assertEquals(CATEGORY_NAME, category.getName());
        assertEquals(DESCRIPTION, category.getDescription());
        assertTrue(category.getProductList().isEmpty());
    }

    @Test
    public void shouldCompareEntitiesWithSameId() {
        // Arrange
        CategoryEntity other = new CategoryEntity();
        other.setId(CATEGORY_ID);

        // Act and assert
        assertEquals(category, other);
        assertEquals(category.hashCode(), other.hashCode());
    }

    @Test
    public void shouldKeepNewEntitiesDistinct() {
        // Entities without an ID represent different new objects.
        assertNotEquals(new CategoryEntity(), new CategoryEntity());
    }

}
