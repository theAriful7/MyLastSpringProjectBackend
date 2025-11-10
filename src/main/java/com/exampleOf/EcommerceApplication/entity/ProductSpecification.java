package com.exampleOf.EcommerceApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_specifications")
public class ProductSpecification {
    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;

    @Column(name = "spec_key", nullable = false) // Column name in database
    private String key; // Example: "color", "size", "weight"

    @Column(name = "spec_value", nullable = false)
    private String value; // Example: "Red", "Large", "1.5kg"

    @Column(name = "display_order")
    private Integer displayOrder = 0; // For sorting specifications on UI

    // Many specifications belong to one product
    @ManyToOne(fetch = FetchType.LAZY) // Don't load product until we need it
    @JoinColumn(name = "product_id", nullable = false) // Foreign key to products table
    private Product product; // Which product this specification belongs to
}
