package com.exampleOf.EcommerceApplication.repository;


import com.exampleOf.EcommerceApplication.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {
    // ✅ BASIC QUERIES
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);

    // ✅ FIND ACTIVE REVIEWS ONLY
    List<Review> findByProductIdAndIsActiveTrue(Long productId);
    List<Review> findByUserIdAndIsActiveTrue(Long userId);

    // ✅ FIND SPECIFIC REVIEW BY USER AND PRODUCT
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // ✅ CHECK IF REVIEW EXISTS (for validation)
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductIdAndIsActiveTrue(Long userId, Long productId);

    // ✅ COUNT REVIEWS
    Long countByProductId(Long productId);
    Long countByProductIdAndIsActiveTrue(Long productId);
    Long countByUserId(Long userId);
    Long countByUserIdAndIsActiveTrue(Long userId);

    // ✅ FIND REVIEWS WITH RATING FILTER
    List<Review> findByProductIdAndRating(Long productId, Integer rating);
    List<Review> findByProductIdAndRatingGreaterThanEqual(Long productId, Integer minRating);
    List<Review> findByProductIdAndRatingLessThanEqual(Long productId, Integer maxRating);

    // ✅ AVERAGE RATING CALCULATION
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isActive = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // ✅ RATING DISTRIBUTION (for charts/analytics)
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isActive = true GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> findRatingDistributionByProductId(@Param("productId") Long productId);

    // ✅ RECENT REVIEWS
    List<Review> findTop5ByProductIdOrderByCreatedAtDesc(Long productId);
    List<Review> findTop10ByOrderByCreatedAtDesc();

    // ✅ FIND REVIEWS WITH PAGINATION SUPPORT (for future use)
    // Page<Review> findByProductId(Long productId, Pageable pageable);
    // Page<Review> findByUserId(Long userId, Pageable pageable);
}
