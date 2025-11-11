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

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {


    // ✅ VENDOR QUERIES
    List<Product> findByVendorId(Long vendorId);
    Page<Product> findByVendorId(Long vendorId, Pageable pageable);
    List<Product> findByVendorIdAndStatus(Long vendorId, ProductStatus status);
    Long countByVendorId(Long vendorId);

    // ✅ CATEGORY QUERIES
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

    // ✅ SUB-CATEGORY QUERIES
    List<Product> findBySubCategoryId(Long subCategoryId);
    Page<Product> findBySubCategoryId(Long subCategoryId, Pageable pageable);

    // ✅ STATUS QUERIES
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // ✅ SEARCH QUERIES
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    // ✅ ADVANCED SEARCH WITH MULTIPLE FIELDS
    @Query("""
        SELECT p FROM Product p 
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')))
        AND p.status = 'ACTIVE'
        AND p.stock > 0
        AND p.vendor.vendorStatus = 'ACTIVE'
        """)
    Page<Product> advancedSearch(@Param("query") String query, Pageable pageable);

    // ✅ COMPREHENSIVE FILTERING WITH PAGINATION
    @Query("""
        SELECT p FROM Product p 
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)
        AND (:vendorId IS NULL OR p.vendor.id = :vendorId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
        AND (:status IS NULL OR p.status = :status)
        AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock = 0))
        AND (:featured IS NULL OR p.isFeatured = :featured)
        AND (:minRating IS NULL OR p.rating >= :minRating)
        AND p.vendor.vendorStatus = 'ACTIVE'
        """)
    Page<Product> findByAdvancedFilters(
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("vendorId") Long vendorId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            @Param("status") ProductStatus status,
            @Param("inStock") Boolean inStock,
            @Param("featured") Boolean featured,
            @Param("minRating") Double minRating,
            Pageable pageable);

    // ✅ SIMPLIFIED FILTERING FOR CONTROLLER
    @Query("""
        SELECT p FROM Product p 
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
        AND (:minRating IS NULL OR p.rating >= :minRating)
        AND (:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock = 0))
        AND p.status = 'ACTIVE'
        AND p.vendor.vendorStatus = 'ACTIVE'
        """)
    Page<Product> findByFilters(
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("brand") String brand,
            @Param("minRating") Double minRating,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    // ✅ TRENDING PRODUCTS (OPTIMIZED)
    @Query(value = """
        SELECT p.* FROM products p
        JOIN vendors v ON p.vendor_id = v.id
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0
        AND v.vendor_status = 'ACTIVE'
        ORDER BY 
            (COALESCE(p.rating, 0) * 0.3 + 
             COALESCE(p.sales_count, 0) * 0.4 + 
             COALESCE(p.view_count, 0) * 0.2 + 
             COALESCE(p.admin_boost, 0) * 0.1 +
             CASE WHEN p.created_at > CURRENT_DATE - INTERVAL '30' DAY THEN 0.1 ELSE 0 END) DESC,
            p.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findTrendingProducts(@Param("limit") int limit);

    // ✅ BEST SELLING PRODUCTS
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0 
        AND p.vendor.vendorStatus = 'ACTIVE'
        ORDER BY p.salesCount DESC, p.rating DESC, p.viewCount DESC
        """)
    List<Product> findBestSellingProducts(Pageable pageable);

    // ✅ FEATURED PRODUCTS
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0 
        AND p.vendor.vendorStatus = 'ACTIVE'
        AND (p.isFeatured = true OR p.rating >= 4.0 OR p.salesCount >= 50)
        ORDER BY 
            CASE WHEN p.isFeatured = true THEN 1 ELSE 0 END DESC,
            p.adminBoost DESC,
            p.salesCount DESC,
            p.rating DESC
        """)
    List<Product> findFeaturedProducts(Pageable pageable);

    // ✅ SIMILAR PRODUCTS (BY CATEGORY & TAGS)
    @Query("""
        SELECT p FROM Product p 
        WHERE p.category.id = :categoryId 
        AND p.id != :excludeProductId
        AND p.status = 'ACTIVE'
        AND p.stock > 0
        AND p.vendor.vendorStatus = 'ACTIVE'
        ORDER BY 
            (CASE WHEN p.brand = :brand THEN 1 ELSE 0 END) DESC,
            p.rating DESC,
            p.salesCount DESC
        """)
    List<Product> findSimilarProducts(@Param("categoryId") Long categoryId,
                                      @Param("excludeProductId") Long excludeProductId,
                                      @Param("brand") String brand,
                                      Pageable pageable);

    // ✅ PRICE RANGE FOR FILTERS
    @Query("""
        SELECT MIN(p.price) as minPrice, MAX(p.price) as maxPrice 
        FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0
        AND p.vendor.vendorStatus = 'ACTIVE'
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        """)
    Object[] findPriceRange(@Param("categoryId") Long categoryId);

    // ✅ AVAILABLE BRANDS FOR FILTERS
    @Query("""
        SELECT DISTINCT p.brand 
        FROM Product p 
        WHERE p.status = 'ACTIVE' 
        AND p.stock > 0
        AND p.vendor.vendorStatus = 'ACTIVE'
        AND p.brand IS NOT NULL
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        ORDER BY p.brand
        """)
    List<String> findAvailableBrands(@Param("categoryId") Long categoryId);

    // ✅ UPDATE METHODS
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.salesCount = COALESCE(p.salesCount, 0) + :quantity WHERE p.id = :productId")
    void incrementSalesCount(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    // ✅ STATISTICS
    Long countByStatus(ProductStatus status);
    Long countByVendorIdAndStatus(Long vendorId, ProductStatus status);

    // ✅ FIND BY MULTIPLE IDs (FOR CART/ORDERS)
    List<Product> findByIdInAndStatus(List<Long> ids, ProductStatus status);

    // ✅ CHECK IF PRODUCT BELONGS TO VENDOR
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.id = :productId AND p.vendor.id = :vendorId")
    boolean existsByIdAndVendorId(@Param("productId") Long productId, @Param("vendorId") Long vendorId);
}