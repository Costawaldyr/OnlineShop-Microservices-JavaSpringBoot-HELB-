package com.onlineshop.catalogue.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "quantity_stock")
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    public ProductEntity( String name, String description, BigDecimal price, Integer stock, CategoryEntity category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ProductEntity)) return false;

        ProductEntity other = (ProductEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product {" + "id=" + id + ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + price + '\'' +
                ", stock=" + stock + '\'' + '}';
    }
}
