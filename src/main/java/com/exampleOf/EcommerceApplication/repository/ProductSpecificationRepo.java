package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecificationRepo extends JpaRepository<ProductSpecification, Long> {
    // Find all specifications for a product
    List<ProductSpecification> findByProductId(Long productId);

    // Delete all specifications for a product
    void deleteByProductId(Long productId);

    // Find specification by product and key
    List<ProductSpecification> findByProductIdAndKey(Long productId, String key);
}
