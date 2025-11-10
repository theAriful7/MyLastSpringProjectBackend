package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Product;
import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> findByVendorId(Long vendorId);
    Page<Product> findByVendorId(Long vendorId, Pageable pageable);
    List<Product> findByVendorIdAndStatus(Long vendorId, ProductStatus status);
    Long countByVendorId(Long vendorId);

    // Category methods
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Status methods
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Filter methods
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    List<Product> findByFilters(@Param("categoryId") Long categoryId,
                                @Param("subCategoryId") Long subCategoryId,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice,
                                @Param("brand") String brand,
                                @Param("status") ProductStatus status);

    // Search methods
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(
            String name, String description, String brand);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(
            String name, String description, String brand, Pageable pageable);

    // ✅ UPDATED: Trending products with VENDOR table
    @Query(value = """
        WITH ranked_products AS (
            SELECT p.*, 
                   v.shop_name as vendor_name,
                   ROW_NUMBER() OVER (PARTITION BY p.vendor_id ORDER BY 
                       (COALESCE(p.rating, 0) * 0.3 + 
                        COALESCE(p.sales_count, 0) * 0.4 + 
                        COALESCE(p.view_count, 0) * 0.2 + 
                        COALESCE(p.admin_boost, 0) * 0.1 +
                        CASE WHEN p.created_at > CURRENT_DATE - INTERVAL 30 DAY THEN 0.1 ELSE 0 END) DESC
                   ) as vendor_rank,
                   (COALESCE(p.rating, 0) * 0.3 + 
                    COALESCE(p.sales_count, 0) * 0.4 + 
                    COALESCE(p.view_count, 0) * 0.2 + 
                    COALESCE(p.admin_boost, 0) * 0.1 +
                    CASE WHEN p.created_at > CURRENT_DATE - INTERVAL 30 DAY THEN 0.1 ELSE 0 END) as performance_score
            FROM products p
            JOIN vendors v ON p.vendor_id = v.id
            WHERE p.status = 'ACTIVE' 
            AND p.stock > 0
            AND v.vendor_status = 'ACTIVE'
        )
        SELECT * FROM ranked_products 
        WHERE vendor_rank <= 2
        ORDER BY performance_score DESC, created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findTrendingProducts(@Param("limit") int limit);

    // ✅ UPDATED: Best sellers with VENDOR
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0 
        AND p.vendor.vendorStatus = 'ACTIVE'
        ORDER BY p.salesCount DESC, p.rating DESC
        """)
    List<Product> findBestSellingProducts(Pageable pageable);

    // ✅ UPDATED: Featured products with VENDOR
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0 
        AND p.vendor.vendorStatus = 'ACTIVE'
        AND (p.isFeatured = true OR p.rating >= 4.0 OR p.salesCount >= 50)
        ORDER BY p.isFeatured DESC, p.adminBoost DESC, p.salesCount DESC
        """)
    List<Product> findFeaturedProducts(Pageable pageable);

    // Update product stats
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.salesCount = COALESCE(p.salesCount, 0) + :quantity WHERE p.id = :productId")
    void incrementSalesCount(@Param("productId") Long productId, @Param("quantity") int quantity);

    // ✅ UPDATED: Vendor distribution with VENDOR table
    @Query("""
        SELECT v.id, v.shopName, 
               COUNT(p.id) as productCount,
               SUM(CASE WHEN p.isFeatured = true THEN 1 ELSE 0 END) as featuredCount
        FROM Vendor v 
        LEFT JOIN v.products p 
        WHERE v.vendorStatus = 'ACTIVE'
        AND (p IS NULL OR p.status = 'ACTIVE')
        GROUP BY v.id, v.shopName
        ORDER BY productCount DESC
        """)
    List<Object[]> getVendorDistributionStats();

    // ✅ NEW: Additional useful methods
    List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);
    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);
    List<Product> findByIsFeaturedTrueAndStatus(ProductStatus status);
    Long countByStatus(ProductStatus status);
}