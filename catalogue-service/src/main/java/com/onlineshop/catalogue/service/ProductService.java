package com.onlineshop.catalogue.service;

import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.exception.InsufficientStockException;
import com.onlineshop.catalogue.exception.InvalidQuantityException;
import com.onlineshop.catalogue.exception.ProductNotFoundException;
import com.onlineshop.catalogue.mapper.CatalogueMapper;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.repository.CategoryRepository;
import com.onlineshop.catalogue.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class ProductService {

    private static final int MINIMUM_ORDER_QUANTITY = 1;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogueMapper catalogueMapper;


    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,CatalogueMapper catalogueMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.catalogueMapper = catalogueMapper;
    }

    /**
     *
     */
    public ProductDto createProduct(ProductDto productDto) {

        CategoryEntity category = categoryRepository.requireById(productDto.categoryId());

        ProductEntity product = new ProductEntity(
                productDto.name(),
                productDto.description(),
                productDto.price(),
                productDto.stock(),
                category
        );

        ProductEntity savedProduct = productRepository.save(product);
        return catalogueMapper.toProductDto(savedProduct);
    }

    /**
     *
     */
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(catalogueMapper::toProductDto)
                .toList();
    }

    /**
     *
     */
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(catalogueMapper::toProductDto);
    }

    /**
     *
     */
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(catalogueMapper::toProductDto)
                .toList();
    }

    /**
     *
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        ProductEntity product = productRepository.requireById(id);
        CategoryEntity category = categoryRepository.requireById(dto.categoryId());

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        product.setCategory(category);

        ProductEntity savedProduct = productRepository.save(product);
        return catalogueMapper.toProductDto(savedProduct);
    }

    /**
     *
     */
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = productRepository.requireById(id);
        productRepository.delete(product);
    }


    @Transactional
    public void increaseStock(Long productId, int quantity) {
        if (quantity < MINIMUM_ORDER_QUANTITY) {
            throw new InvalidQuantityException(quantity);
        }

        int updatedRows = productRepository.increaseStock(productId, quantity);

        if (updatedRows == 0) {
            throw new ProductNotFoundException(productId);
        }
    }

    /**
     *
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        if (quantity <  MINIMUM_ORDER_QUANTITY) {
            throw new InvalidQuantityException(quantity);
        }

        int updatedRows = productRepository.decreaseStock(productId, quantity);

        if (updatedRows == 0) {
            productRepository.requireById(productId);
            throw new InsufficientStockException(productId);
        }
    }
}
