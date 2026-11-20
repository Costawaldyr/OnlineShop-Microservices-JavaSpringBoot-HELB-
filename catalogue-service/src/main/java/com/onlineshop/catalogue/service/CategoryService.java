package com.onlineshop.catalogue.service;


import com.onlineshop.catalogue.dto.CategoryDto;
import com.onlineshop.catalogue.exception.CategoryAlreadyExistsException;
import com.onlineshop.catalogue.mapper.CatalogueMapper;
import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repo;
    private final CatalogueMapper mapper;

    public CategoryService(CategoryRepository repo, CatalogueMapper catalogueMapper) {
        this.repo = repo;
        this.mapper = catalogueMapper;
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {

        String name = categoryDto.name().trim();
        if (repo.existsByNameIgnoreCase(name)) {
            throw new CategoryAlreadyExistsException(name);
        }

        CategoryEntity category = new CategoryEntity(
                categoryDto.name(),
                categoryDto.description()
        );

        CategoryEntity savedCategory = repo.save(category);
        return mapper.toCategoryDto(savedCategory);
    }

    public List<CategoryDto> getAllCategories() {
        return repo.findAll()
                .stream()
                .map(mapper::toCategoryDto)
                .toList();
    }

    public CategoryDto getCategoryById(Long id) {
        return mapper.toCategoryDto(repo.requireById(id));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        CategoryEntity category = repo.requireById(id);

        category.setName(categoryDto.name());
        category.setDescription(categoryDto.description());

        CategoryEntity savedCategory = repo.save(category);
        return mapper.toCategoryDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = repo.requireById(id);

        if (!category.getProductList().isEmpty()) {
            throw new IllegalStateException("Cannot delete a category with products: " + id );
        }
        repo.delete(category);
    }
}
