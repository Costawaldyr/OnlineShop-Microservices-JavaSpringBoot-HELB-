package com.onlineshop.catalogue.serviceTest;

import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.mapper.CatalogueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import com.onlineshop.catalogue.exception.CategoryAlreadyExistsException;
import com.onlineshop.catalogue.exception.CategoryNotFoundException;
import com.onlineshop.catalogue.repository.CategoryRepository;
import com.onlineshop.catalogue.service.CategoryService;
import com.onlineshop.catalogue.exception.CategoryNotEmptyException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    private static final Long CATEGORY_ID = 2L;
    private static final String CATEGORY_NAME = "Computing";
    private static final String CATEGORY_DESCRIPTION = "Computer equipment";
    private static final String UPDATED_NAME = "Gaming";
    private static final String UPDATED_DESCRIPTION = "Gaming accessories";
    private static final String PADDED_NAME = "  Computing  ";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogueMapper catalogueMapper;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryEntity category;
    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        category = new CategoryEntity(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        category.setId(CATEGORY_ID);
        categoryDto = new CategoryDto(CATEGORY_ID, CATEGORY_NAME, CATEGORY_DESCRIPTION, List.of());
    }

    @Test
    public void shouldReturnCategoryWhenIdExists() {

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(catalogueMapper.toCategoryDto(category)).thenReturn(categoryDto);

        Optional<CategoryDto> foundCategory = categoryService.getCategoryById(CATEGORY_ID);

        assertTrue(foundCategory.isPresent());
        assertEquals(categoryDto, foundCategory.get());
        verify(categoryRepository).findById(CATEGORY_ID);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        Optional<CategoryDto> foundCategory = categoryService.getCategoryById(CATEGORY_ID);
        assertTrue(foundCategory.isEmpty());
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldReturnAllCategories() {

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(catalogueMapper.toCategoryDto(category)).thenReturn(categoryDto);
        List<CategoryDto> foundCategories = categoryService.getAllCategories();
        assertEquals(List.of(categoryDto), foundCategories);
        verify(categoryRepository).findAll();
    }

    @Test
    public void shouldReturnEmptyListWhenNoCategoriesExist() {

        when(categoryRepository.findAll()).thenReturn(List.of());
        List<CategoryDto> foundCategories = categoryService.getAllCategories();
        assertTrue(foundCategories.isEmpty());
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldCreateCategoryWithTrimmedName() {

        CategoryDto requestDto = new CategoryDto(CATEGORY_ID, PADDED_NAME, CATEGORY_DESCRIPTION, List.of());
        when(categoryRepository.existsByNameIgnoreCase(CATEGORY_NAME)).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(category);
        when(catalogueMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CategoryDto createdCategory = categoryService.createCategory(requestDto);

        assertEquals(categoryDto, createdCategory);

        // Capture the entity passed to save to check the trimmed name.
        ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);

        verify(categoryRepository).save(captor.capture());
        assertEquals(CATEGORY_NAME, captor.getValue().getName());
        assertEquals(CATEGORY_DESCRIPTION, captor.getValue().getDescription());
        assertNull(captor.getValue().getId());
        assertTrue(captor.getValue().getProductList().isEmpty());
    }

    @Test
    public void shouldRejectDuplicateCategory() {

        when(categoryRepository.existsByNameIgnoreCase(CATEGORY_NAME)).thenReturn(true);
        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(categoryDto));
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldUpdateExistingCategory() {

        // Arrange
        CategoryDto requestDto = new CategoryDto(CATEGORY_ID, UPDATED_NAME, UPDATED_DESCRIPTION, List.of());
        when(categoryRepository.requireById(CATEGORY_ID)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(catalogueMapper.toCategoryDto(category)).thenReturn(requestDto);

        // Act
        CategoryDto updatedCategory = categoryService.updateCategory(CATEGORY_ID, requestDto);

        // Assert
        assertEquals(requestDto, updatedCategory);
        assertEquals(UPDATED_NAME, category.getName());
        assertEquals(UPDATED_DESCRIPTION, category.getDescription());
        assertEquals(CATEGORY_ID, category.getId());
        verify(categoryRepository).save(category);
    }

    @Test
    public void shouldRejectUpdateWhenCategoryDoesNotExist() {

        when(categoryRepository.requireById(CATEGORY_ID)).thenThrow(new CategoryNotFoundException(CATEGORY_ID));
        assertThrows(CategoryNotFoundException.class,() -> categoryService.updateCategory(CATEGORY_ID, categoryDto));
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    public void shouldDeleteEmptyCategory() {

        when(categoryRepository.requireById(CATEGORY_ID)).thenReturn(category);
        categoryService.deleteCategory(CATEGORY_ID);
        verify(categoryRepository).delete(category);
    }

    @Test
    public void shouldRejectDeletionWhenCategoryContainsProducts() {

        category.getProductList().add(new ProductEntity());
        when(categoryRepository.requireById(CATEGORY_ID)).thenReturn(category);
        assertThrows(CategoryNotEmptyException.class, () -> categoryService.deleteCategory(CATEGORY_ID));
        verify(categoryRepository, never()).delete(any(CategoryEntity.class));
    }

    @Test
    public void shouldRejectDeletionWhenCategoryDoesNotExist() {

        when(categoryRepository.requireById(CATEGORY_ID)).thenThrow(new CategoryNotFoundException(CATEGORY_ID));
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(CATEGORY_ID));
        verify(categoryRepository, never()).delete(any(CategoryEntity.class));
    }
}
