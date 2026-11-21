package com.onlineshop.catalogue.controller;


import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.dto.ErrorResponseDto;
import com.onlineshop.catalogue.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Category management")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Create category")
    @ApiResponse(responseCode = "201", description = "Category created", content = @Content(schema = @Schema(implementation = CategoryDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid category data", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody CategoryDto categoryDto) {
        return categoryService.createCategory(categoryDto);
    }

    @Operation(summary = "Get all categories")
    @GetMapping
    public List<CategoryDto> getAll() {
        return categoryService.getAllCategories();
    }

    @Operation(summary = "Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable("id") Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update category")
    @ApiResponse(responseCode = "200", description = "Category updated", content = @Content(schema = @Schema(implementation = CategoryDto.class)))
    @ApiResponse(responseCode = "404", description = "Category not found",content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@PathVariable("id") Long id,@Valid @RequestBody CategoryDto categoryDto) {
        return categoryService.updateCategory(id, categoryDto);
    }

    @Operation(summary = "Delete category")
    @ApiResponse(responseCode = "204", description = "Category deleted", content = @Content(schema = @Schema(implementation = Void.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
    }
}
