package com.onlineshop.catalogue.repository;

import com.onlineshop.catalogue.entity.CategoryEntity;
import com.onlineshop.catalogue.exception.CategoryNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    boolean existsByNameIgnoreCase(String name);

    public default CategoryEntity requireById(Long id) {
        return findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
