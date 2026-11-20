package com.onlineshop.catalogue.controller;


import com.onlineshop.catalogue.dto.ProductDto;
import com.onlineshop.catalogue.service.ProductService;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @GetMapping
    public List<ProductDto> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/by-category/{categoryId}")
    public List<ProductDto> getByCategory(@PathVariable("categoryId") Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable("id") Long id, @RequestBody ProductDto productDto ) {
        return productService.updateProduct(id, productDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/{id}/stock/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseStock( @PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        productService.decreaseStock(id, quantity);
    }
}
