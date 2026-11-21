package com.onlineshop.catalogue.repository;

import com.onlineshop.catalogue.entity.ProductEntity;
import com.onlineshop.catalogue.exception.ProductNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    public default ProductEntity requireById(Long id) {
        return findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductEntity> findByCategoryId(Long categoryId);

    @Modifying
    @Query(" update ProductEntity p set p.stock = p.stock - :quantity where p.id = :productId and p.stock >= :quantity ")
    public int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query(" update ProductEntity p set p.stock = p.stock + :quantity where p.id = :productId ")
    public int increaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
