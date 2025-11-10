package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.FileData;
import com.exampleOf.EcommerceApplication.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileDataRepo extends JpaRepository<FileData, Long> {
    List<FileData> findByProductId(Long productId);

    List<FileData> findByProductIdAndIsActiveTrue(Long productId);

    Optional<FileData> findByProductIdAndIsPrimaryTrue(Long productId);

    @Query("SELECT fd FROM FileData fd WHERE fd.product.id = :productId ORDER BY fd.sortOrder ASC")
    List<FileData> findProductImagesOrdered(@Param("productId") Long productId);

    Optional<FileData> findByFileName(String fileName);

    Optional<FileData> findByChecksum(String checksum);

    @Modifying
    @Query("UPDATE FileData f SET f.isPrimary = false WHERE f.product.id = :productId")
    void resetPrimaryImages(@Param("productId") Long productId);

    boolean existsByProductAndIsPrimary(Product product, Boolean isPrimary);

    @Query("SELECT MAX(f.sortOrder) FROM FileData f WHERE f.product.id = :productId")
    Integer findMaxSortOrderByProductId(@Param("productId") Long productId);

}
