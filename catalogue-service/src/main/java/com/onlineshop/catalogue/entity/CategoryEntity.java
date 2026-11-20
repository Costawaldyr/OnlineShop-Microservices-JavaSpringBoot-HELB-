package com.onlineshop.catalogue.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;

    @OneToMany(mappedBy = "category")
    private List<ProductEntity> productList = new ArrayList<>();

    public CategoryEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CategoryEntity)) return false;

        CategoryEntity other = (CategoryEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Category {" + "id=" + id + ", name='" + name + '\'' + ", description='" + description + '\'' + ", Products=" + productList + '}';
    }
}
