package com.onlineshop.catalogue.serviceTest;

import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.exception.CategoryNotFoundException;
import com.onlineshop.catalogue.exception.ProductNotFoundException;
import com.onlineshop.catalogue.mapper.CatalogueMapper;
import com.onlineshop.catalogue.repository.CategoryRepository;
import com.onlineshop.catalogue.repository.ProductRepository;
import com.onlineshop.catalogue.service.ProductService;
import com.onlineshop.catalogue.exception.InsufficientStockException;
import com.onlineshop.catalogue.exception.InvalidQuantityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long SECOND_PRODUCT_ID = 3L;
    private static final Long CATEGORY_ID = 2L;
    private static final Long NEW_CATEGORY_ID = 4L;

    private static final String CATEGORY_NAME = "Computing";
    private static final String CATEGORY_DESCRIPTION = "Computer equipment";

    private static final String NEW_CATEGORY_NAME = "Gaming";
    private static final String NEW_CATEGORY_DESCRIPTION = "Gaming accessories";

    private static final String PRODUCT_NAME = "Keyboard";
    private static final String PRODUCT_DESCRIPTION = "USB keyboard";
    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("29.99");
    private static final int INITIAL_STOCK = 10;

    private static final String SECOND_PRODUCT_NAME = "Mouse";
    private static final String SECOND_PRODUCT_DESCRIPTION = "USB mouse";
    private static final BigDecimal SECOND_PRODUCT_PRICE = new BigDecimal("19.99");
    private static final int SECOND_PRODUCT_STOCK = 5;

    private static final String UPDATED_PRODUCT_NAME = "Mechanical keyboard";
    private static final String UPDATED_PRODUCT_DESCRIPTION = "Mechanical USB keyboard";
    private static final BigDecimal UPDATED_PRODUCT_PRICE = new BigDecimal("79.99");
    private static final int UPDATED_PRODUCT_STOCK = 20;

    private static final int REQUESTED_QUANTITY = 2;
    private static final int ZERO_QUANTITY = 0;
    private static final int NEGATIVE_QUANTITY = -1;
    private static final int QUANTITY_EXCEEDING_STOCK = INITIAL_STOCK + 1;

    private static final int ONE_UPDATED_ROW = 1;
    private static final int NO_UPDATED_ROWS = 0;

    private static final String INSUFFICIENT_STOCK_MESSAGE = "Insufficient stock for product: " + PRODUCT_ID;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogueMapper catalogueMapper;

    private ProductService productService;

    private CategoryEntity category;
    private ProductEntity product;
    private ProductDto productDto;

    @BeforeEach
    public void setUp() {
        productService = new ProductService(
                productRepository,
                categoryRepository,
                catalogueMapper
        );

        // Each test receives fresh entities.
        category = new CategoryEntity(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        category.setId(CATEGORY_ID);

        product = new ProductEntity(
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                INITIAL_STOCK,
                category
        );
        product.setId(PRODUCT_ID);

        productDto = new ProductDto(
                PRODUCT_ID,
                PRODUCT_NAME,
                PRODUCT_DESCRIPTION,
                PRODUCT_PRICE,
                INITIAL_STOCK,
                CATEGORY_ID
        );
    }

    @Test
    public void shouldReturnProductWhenIdExists() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(catalogueMapper.toProductDto(product)).thenReturn(productDto);

        // Act
        Optional<ProductDto> foundProduct = productService.getProductById(PRODUCT_ID);

        // Assert
        assertTrue(foundProduct.isPresent());
        assertEquals(productDto, foundProduct.get());
        assertEquals(PRODUCT_NAME, foundProduct.get().name());

        verify(productRepository).findById(PRODUCT_ID);
        verify(catalogueMapper).toProductDto(product);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenProductDoesNotExist() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        Optional<ProductDto> foundProduct = productService.getProductById(PRODUCT_ID);

        assertTrue(foundProduct.isEmpty());

        verify(productRepository).findById(PRODUCT_ID);
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldReturnAllProducts() {

        ProductEntity secondProduct = new ProductEntity(
                SECOND_PRODUCT_NAME,
                SECOND_PRODUCT_DESCRIPTION,
                SECOND_PRODUCT_PRICE,
                SECOND_PRODUCT_STOCK,
                category
        );
        secondProduct.setId(SECOND_PRODUCT_ID);

        ProductDto secondProductDto = new ProductDto(
                SECOND_PRODUCT_ID,
                SECOND_PRODUCT_NAME,
                SECOND_PRODUCT_DESCRIPTION,
                SECOND_PRODUCT_PRICE,
                SECOND_PRODUCT_STOCK,
                CATEGORY_ID
        );

        when(productRepository.findAll()).thenReturn(List.of(product, secondProduct));
        when(catalogueMapper.toProductDto(product)).thenReturn(productDto);
        when(catalogueMapper.toProductDto(secondProduct)).thenReturn(secondProductDto);

        List<ProductDto> foundProducts = productService.getAllProducts();

        assertEquals(List.of(productDto, secondProductDto), foundProducts);

        verify(productRepository).findAll();
        verify(catalogueMapper).toProductDto(product);
        verify(catalogueMapper).toProductDto(secondProduct);
    }

    @Test
    public void shouldReturnEmptyListWhenNoProductsExist() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        List<ProductDto> foundProducts = productService.getAllProducts();

        // Assert
        assertTrue(foundProducts.isEmpty());

        verify(productRepository).findAll();
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldReturnProductsForCategory() {
        // Arrange
        when(productRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of(product));

        when(catalogueMapper.toProductDto(product))
                .thenReturn(productDto);

        // Act
        List<ProductDto> foundProducts =
                productService.getProductsByCategory(CATEGORY_ID);

        // Assert
        assertEquals(List.of(productDto), foundProducts);

        verify(productRepository).findByCategoryId(CATEGORY_ID);
        verify(catalogueMapper).toProductDto(product);
    }

    @Test
    public void shouldReturnEmptyListWhenCategoryHasNoProducts() {
        // Arrange
        when(productRepository.findByCategoryId(CATEGORY_ID))
                .thenReturn(List.of());

        // Act
        List<ProductDto> foundProducts =
                productService.getProductsByCategory(CATEGORY_ID);

        // Assert
        assertTrue(foundProducts.isEmpty());

        verify(productRepository).findByCategoryId(CATEGORY_ID);
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldCreateProductWhenCategoryExists() {
        // Arrange
        when(categoryRepository.requireById(CATEGORY_ID))
                .thenReturn(category);

        when(productRepository.save(any(ProductEntity.class)))
                .thenReturn(product);

        when(catalogueMapper.toProductDto(product))
                .thenReturn(productDto);

        // Act
        ProductDto createdProduct = productService.createProduct(productDto);

        // Assert
        assertEquals(productDto, createdProduct);

        // Capture the entity passed to save so its fields can be checked.
        ArgumentCaptor<ProductEntity> productCaptor =
                ArgumentCaptor.forClass(ProductEntity.class);

        verify(productRepository).save(productCaptor.capture());

        ProductEntity savedEntity = productCaptor.getValue();

        // The incoming DTO ID must not be copied into a new entity.
        assertNull(savedEntity.getId());

        assertEquals(PRODUCT_NAME, savedEntity.getName());
        assertEquals(PRODUCT_DESCRIPTION, savedEntity.getDescription());
        assertEquals(PRODUCT_PRICE, savedEntity.getPrice());
        assertEquals(
                Integer.valueOf(INITIAL_STOCK),
                savedEntity.getStock()
        );
        assertSame(category, savedEntity.getCategory());

        verify(categoryRepository).requireById(CATEGORY_ID);
        verify(catalogueMapper).toProductDto(product);
    }

    @Test
    public void shouldRejectCreationWhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.requireById(CATEGORY_ID))
                .thenThrow(new CategoryNotFoundException(CATEGORY_ID));

        // Act and Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.createProduct(productDto)
        );

        // Creation stops before any product is saved.
        verifyNoInteractions(productRepository, catalogueMapper);
    }

    @Test
    public void shouldUpdateProductWhenProductAndCategoryExist() {
        // Arrange
        CategoryEntity newCategory = new CategoryEntity(
                NEW_CATEGORY_NAME,
                NEW_CATEGORY_DESCRIPTION
        );
        newCategory.setId(NEW_CATEGORY_ID);

        ProductDto updatedProductDto = new ProductDto(
                PRODUCT_ID,
                UPDATED_PRODUCT_NAME,
                UPDATED_PRODUCT_DESCRIPTION,
                UPDATED_PRODUCT_PRICE,
                UPDATED_PRODUCT_STOCK,
                NEW_CATEGORY_ID
        );

        when(productRepository.requireById(PRODUCT_ID))
                .thenReturn(product);

        when(categoryRepository.requireById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(productRepository.save(product))
                .thenReturn(product);

        when(catalogueMapper.toProductDto(product))
                .thenReturn(updatedProductDto);

        // Act
        ProductDto updatedProduct =
                productService.updateProduct(PRODUCT_ID, updatedProductDto);

        // Assert
        assertEquals(updatedProductDto, updatedProduct);

        // The existing entity receives the new values and keeps its ID.
        assertEquals(PRODUCT_ID, product.getId());
        assertEquals(UPDATED_PRODUCT_NAME, product.getName());
        assertEquals(
                UPDATED_PRODUCT_DESCRIPTION,
                product.getDescription()
        );
        assertEquals(UPDATED_PRODUCT_PRICE, product.getPrice());
        assertEquals(
                Integer.valueOf(UPDATED_PRODUCT_STOCK),
                product.getStock()
        );
        assertSame(newCategory, product.getCategory());

        verify(productRepository).save(product);
        verify(catalogueMapper).toProductDto(product);
    }

    @Test
    public void shouldRejectUpdateWhenProductDoesNotExist() {
        // Arrange
        when(productRepository.requireById(PRODUCT_ID))
                .thenThrow(new ProductNotFoundException(PRODUCT_ID));

        // Act and Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(PRODUCT_ID, productDto)
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
        verifyNoInteractions(categoryRepository, catalogueMapper);
    }

    @Test
    public void shouldRejectUpdateWhenCategoryDoesNotExist() {
        // Arrange
        when(productRepository.requireById(PRODUCT_ID))
                .thenReturn(product);

        when(categoryRepository.requireById(CATEGORY_ID))
                .thenThrow(new CategoryNotFoundException(CATEGORY_ID));

        // Act and Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.updateProduct(PRODUCT_ID, productDto)
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
        verifyNoInteractions(catalogueMapper);
    }

    @Test
    public void shouldDeleteProductWhenIdExists() {
        when(productRepository.requireById(PRODUCT_ID)).thenReturn(product);

        productService.deleteProduct(PRODUCT_ID);

        verify(productRepository).requireById(PRODUCT_ID);
        verify(productRepository).delete(product);
    }

    @Test
    public void shouldRejectDeletionWhenProductDoesNotExist() {
        when(productRepository.requireById(PRODUCT_ID)).thenThrow(new ProductNotFoundException(PRODUCT_ID));

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(PRODUCT_ID)
        );

        verify(productRepository, never()).delete(any(ProductEntity.class));
    }

    @Test
    public void shouldDecreaseStockWhenQuantityIsAvailable() {

        when(productRepository.decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY)).thenReturn(ONE_UPDATED_ROW);

        productService.decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY);

        verify(productRepository).decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY);
        verify(productRepository, never()).requireById(PRODUCT_ID);
    }

    @Test
    public void shouldRejectZeroQuantity() {
        assertThrows(
                InvalidQuantityException.class,
                () -> productService.decreaseStock( PRODUCT_ID,ZERO_QUANTITY)
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    public void shouldRejectNegativeQuantity() {
        assertThrows(
                InvalidQuantityException.class,
                () -> productService.decreaseStock( PRODUCT_ID,NEGATIVE_QUANTITY )
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    public void shouldRejectDecreaseWhenStockIsInsufficient() {

        when(productRepository.decreaseStock(PRODUCT_ID, QUANTITY_EXCEEDING_STOCK)).thenReturn(NO_UPDATED_ROWS);
        when(productRepository.requireById(PRODUCT_ID)).thenReturn(product);

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> productService.decreaseStock( PRODUCT_ID,QUANTITY_EXCEEDING_STOCK)
        );

        assertEquals(INSUFFICIENT_STOCK_MESSAGE, exception.getMessage());

        verify(productRepository).decreaseStock(PRODUCT_ID, QUANTITY_EXCEEDING_STOCK);
        verify(productRepository).requireById(PRODUCT_ID);
    }

    @Test
    public void shouldRejectDecreaseWhenProductDoesNotExist() {

        when(productRepository.decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY)).thenReturn(NO_UPDATED_ROWS);
        when(productRepository.requireById(PRODUCT_ID)).thenThrow(new ProductNotFoundException(PRODUCT_ID));


        assertThrows(
                ProductNotFoundException.class,
                () -> productService.decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY)
        );

        verify(productRepository).decreaseStock(PRODUCT_ID, REQUESTED_QUANTITY);
        verify(productRepository).requireById(PRODUCT_ID);
    }
}
