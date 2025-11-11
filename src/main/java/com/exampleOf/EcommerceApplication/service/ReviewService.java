package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ValidationException;
import com.exampleOf.EcommerceApplication.dto.requestdto.ReviewRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.ReviewResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Product;
import com.exampleOf.EcommerceApplication.entity.Review;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.repository.ProductRepo;
import com.exampleOf.EcommerceApplication.repository.ReviewRepo;
import com.exampleOf.EcommerceApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final UserRepository userRepo;
    private final ProductRepo productRepo;

    // ✅ Create Review with validation
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {
        try {
            // Validate user exists
            User user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

            // Validate product exists
            Product product = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

            // Validate rating (1-5)
            if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
                throw new ValidationException("rating", "Rating must be between 1 and 5");
            }

            // Validate comment
            if (dto.getComment() == null || dto.getComment().trim().isEmpty()) {
                throw new ValidationException("comment", "Comment is required");
            }

            // ✅ UPDATED: Use efficient repository method for duplicate check
            boolean alreadyReviewed = reviewRepo.existsByUserIdAndProductIdAndIsActiveTrue(
                    dto.getUserId(), dto.getProductId()
            );

            if (alreadyReviewed) {
                throw new OperationFailedException(
                        "Create review",
                        "You have already reviewed this product"
                );
            }

            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setComment(dto.getComment().trim());
            review.setRating(dto.getRating());
            review.setIsActive(true);

            Review saved = reviewRepo.save(review);

            // Update product average rating
            updateProductAverageRating(product.getId());

            return convertToResponse(saved);
        } catch (Exception ex) {
            throw new OperationFailedException("Create review", ex.getMessage());
        }
    }

    // ✅ Get All Reviews
    public List<ReviewResponseDTO> getAllReviews() {
        try {
            return reviewRepo.findAll()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve all reviews", ex.getMessage());
        }
    }

    // ✅ Get Reviews by Product
    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            // ✅ UPDATED: Use repository method that filters active reviews
            return reviewRepo.findByProductIdAndIsActiveTrue(productId)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve product reviews", ex.getMessage());
        }
    }

    // ✅ Get Reviews by User
    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        try {
            // Verify user exists
            if (!userRepo.existsById(userId)) {
                throw new ResourceNotFoundException("User", "id", userId);
            }

            // ✅ UPDATED: Use repository method that filters active reviews
            return reviewRepo.findByUserIdAndIsActiveTrue(userId)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve user reviews", ex.getMessage());
        }
    }

    // ✅ Get Review by ID
    public ReviewResponseDTO getReviewById(Long id) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (!review.getIsActive()) {
            throw new ResourceNotFoundException("Review", "id", id);
        }

        return convertToResponse(review);
    }

    // ✅ Update Review
    @Transactional
    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO dto) {
        try {
            Review existing = reviewRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

            // Validate rating if provided
            if (dto.getRating() != null) {
                if (dto.getRating() < 1 || dto.getRating() > 5) {
                    throw new ValidationException("rating", "Rating must be between 1 and 5");
                }
                existing.setRating(dto.getRating());
            }

            // Update comment if provided
            if (dto.getComment() != null && !dto.getComment().trim().isEmpty()) {
                existing.setComment(dto.getComment().trim());
            }

            Review updated = reviewRepo.save(existing);

            // Update product average rating
            updateProductAverageRating(updated.getProduct().getId());

            return convertToResponse(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Update review", ex.getMessage());
        }
    }

    // ✅ Soft Delete Review (Set inactive instead of hard delete)
    @Transactional
    public void deleteReview(Long id) {
        try {
            Review review = reviewRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

            review.setIsActive(false);
            reviewRepo.save(review);

            // Update product average rating
            updateProductAverageRating(review.getProduct().getId());
        } catch (Exception ex) {
            throw new OperationFailedException("Delete review", ex.getMessage());
        }
    }

    // ✅ Get Product Average Rating
    public Double getProductAverageRating(Long productId) {
        try {
            // ✅ UPDATED: Use direct repository method for average calculation
            Double average = reviewRepo.findAverageRatingByProductId(productId);
            return average != null ? Math.round(average * 10.0) / 10.0 : 0.0; // Round to 1 decimal
        } catch (Exception ex) {
            throw new OperationFailedException("Calculate average rating", ex.getMessage());
        }
    }

    // ✅ NEW: Get Recent Reviews for Product
    public List<ReviewResponseDTO> getRecentReviewsByProduct(Long productId, int limit) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            List<Review> recentReviews = reviewRepo.findTop5ByProductIdOrderByCreatedAtDesc(productId);
            return recentReviews.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve recent reviews", ex.getMessage());
        }
    }

    // ✅ NEW: Get Reviews by Rating
    public List<ReviewResponseDTO> getReviewsByRating(Long productId, Integer rating) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }
            // Validate rating
            if (rating < 1 || rating > 5) {
                throw new ValidationException("rating", "Rating must be between 1 and 5");
            }

            List<Review> reviews = reviewRepo.findByProductIdAndRating(productId, rating);
            return reviews.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve reviews by rating", ex.getMessage());
        }
    }

    // ✅ NEW: Get Review Statistics
    public ReviewStatsDTO getReviewStatistics(Long productId) {
        try {
            // Verify product exists
            if (!productRepo.existsById(productId)) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }

            ReviewStatsDTO stats = new ReviewStatsDTO();
            stats.setTotalReviews(reviewRepo.countByProductIdAndIsActiveTrue(productId));
            stats.setAverageRating(getProductAverageRating(productId));

            // Get rating distribution
            List<Object[]> distribution = reviewRepo.findRatingDistributionByProductId(productId);
            // Process distribution data as needed

            return stats;
        } catch (Exception ex) {
            throw new OperationFailedException("Get review statistics", ex.getMessage());
        }
    }

    // ✅ NEW: Get User's Review for Product
    public ReviewResponseDTO getUserReviewForProduct(Long userId, Long productId) {
        try {
            Review review = reviewRepo.findByUserIdAndProductId(userId, productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Review", "user and product", "User ID: " + userId + ", Product ID: " + productId
                    ));

            if (!review.getIsActive()) {
                throw new ResourceNotFoundException(
                        "Review", "user and product", "User ID: " + userId + ", Product ID: " + productId
                );
            }

            return convertToResponse(review);
        } catch (Exception ex) {
            throw new OperationFailedException("Get user review for product", ex.getMessage());
        }
    }

    // ✅ Helper method to update product average rating
    private void updateProductAverageRating(Long productId) {
        try {
            Double averageRating = getProductAverageRating(productId);
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

            // If your Product entity has an averageRating field, update it here
            // product.setAverageRating(averageRating);
            // productRepo.save(product);
        } catch (Exception ex) {
            // Log error but don't throw - this is a background update
            System.err.println("Failed to update product average rating: " + ex.getMessage());
        }
    }

    // ✅ Convert Entity to ResponseDTO
    private ReviewResponseDTO convertToResponse(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setUserName(review.getUser().getFirstName());
        dto.setProductName(review.getProduct().getName());
        dto.setIsActive(review.getIsActive());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    // ✅ NEW: DTO for Review Statistics
    public static class ReviewStatsDTO {
        private Long totalReviews;
        private Double averageRating;

        // Getters and setters
        public Long getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(Long totalReviews) {
            this.totalReviews = totalReviews;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }
    }
}
