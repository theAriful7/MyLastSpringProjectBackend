package com.exampleOf.EcommerceApplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sub_categories")
@EqualsAndHashCode(callSuper = true)
public class SubCategory extends Base{
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // Each sub-category belongs to one main category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Products can belong to sub-categories
    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // Helper method
    public void addProduct(Product product) {
        products.add(product);
        product.setSubCategory(this);
    }
}
