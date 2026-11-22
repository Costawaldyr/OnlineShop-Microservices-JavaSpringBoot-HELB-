package com.onlineshop.catalogue.controller;


import com.onlineshop.catalogue.dto.ErrorResponseDto;
import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "Products", description = "Product catalogue and stock management")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@Valid @RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public List<ProductDto> getAll() {
        return productService.getAllProducts();
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get products by category id")
    @GetMapping("/by-category/{categoryId}")
    public List<ProductDto> getByCategory(@PathVariable("categoryId") Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @Operation(summary = "Update product")
    @ApiResponse(responseCode = "200", description = "Product updated", content = @Content(schema = @Schema(implementation = ProductDto.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PutMapping("/{id}")
    public ProductDto update(@PathVariable("id") Long id,@Valid @RequestBody ProductDto productDto ) {
        return productService.updateProduct(id, productDto);
    }

    @Operation(summary = "Delete product")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
    }

    @Operation(summary = "Increase product stock")
    @ApiResponse(responseCode = "204", description = "Stock increased")
    @PostMapping("/{id}/stock/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increaseStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        productService.increaseStock(id, quantity);
    }

    @Operation(summary = "Decrease product stock")
    @ApiResponse(responseCode = "204", description = "Stock decreased")
    @ApiResponse(responseCode = "400", description = "Quantity must be at least 1", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Product not found",content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "Insufficient stock",content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/{id}/stock/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseStock( @PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        productService.decreaseStock(id, quantity);
    }
}
