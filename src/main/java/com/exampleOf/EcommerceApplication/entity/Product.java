package com.exampleOf.EcommerceApplication.entity;


import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@EqualsAndHashCode(callSuper = true)
@Builder
public class Product extends Base{

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    private Double discount;

    private String brand;



    @Column(name = "sales_count")
    @Builder.Default
    private Integer salesCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "admin_boost")
    @Builder.Default
    private Double adminBoost = 0.0;

//    @Column(unique = true, nullable = false)
//    private String sku;


    // One-to-Many with FileData for images (replacing old List<String> imageUrls)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FileData> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // NEW: Optional sub-category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory; // ADD THIS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false) // Changed from user_id to vendor_id
    private Vendor vendor;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpecification> specifications = new ArrayList<>();


    public void addSpecification(String key, String value, Integer displayOrder) {
        ProductSpecification spec = new ProductSpecification();
        spec.setKey(key);
        spec.setValue(value);
        spec.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        spec.setProduct(this); // Very important - connect specification to product
        this.specifications.add(spec);
    }
}
