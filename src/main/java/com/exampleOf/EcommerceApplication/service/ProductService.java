package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.UnauthorizedAccessException;
import com.exampleOf.EcommerceApplication.dto.requestdto.FileDataDTO;
import com.exampleOf.EcommerceApplication.dto.requestdto.ProductRequestDTO;
import com.exampleOf.EcommerceApplication.dto.requestdto.ProductSpecificationDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.ProductResponseDTO;
import com.exampleOf.EcommerceApplication.entity.*;
import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import com.exampleOf.EcommerceApplication.enums.VendorStatus;
import com.exampleOf.EcommerceApplication.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final VendorRepository vendorRepo;
    private final ProductSpecificationRepo specificationRepo;
    private final SubCategoryRepo subCategoryRepo;
    private final FileDataRepo fileDataRepo;

    // ==================== MAPPING METHODS ====================

    private Product toEntity(ProductRequestDTO dto, Category category, Vendor vendor) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImages(new ArrayList<>());
        product.setCategory(category);
        product.setVendor(vendor);
        product.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : 0.0);
        product.setBrand(dto.getBrand());
        product.setStatus(ProductStatus.ACTIVE);

        // Handle specifications
        if (dto.getSpecifications() != null && !dto.getSpecifications().isEmpty()) {
            for (ProductSpecificationDTO specDTO : dto.getSpecifications()) {
                ProductSpecification specification = new ProductSpecification();
                specification.setKey(specDTO.getKey());
                specification.setValue(specDTO.getValue());
                specification.setDisplayOrder(specDTO.getDisplayOrder() != null ? specDTO.getDisplayOrder() : 0);
                specification.setProduct(product);
                product.getSpecifications().add(specification);
            }
        }

        // Handle sub-category if provided
        if (dto.getSubCategoryId() != null) {
            SubCategory subCategory = subCategoryRepo.findById(dto.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", dto.getSubCategoryId()));

            if (!subCategory.getCategory().getId().equals(category.getId())) {
                throw new OperationFailedException(
                        "Create product",
                        "Sub-category does not belong to the selected category"
                );
            }
            product.setSubCategory(subCategory);
        }

        return product;
    }

    private ProductResponseDTO toDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());

        // Map images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<FileDataDTO> imageDTOs = product.getImages().stream()
                    .map(this::mapFileDataToDTO)
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        } else {
            dto.setImages(new ArrayList<>());
        }

        dto.setDiscount(product.getDiscount());
        dto.setBrand(product.getBrand());
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setStatus(product.getStatus());
        dto.setVendorId(product.getVendor() != null ? product.getVendor().getId() : null);
        dto.setVendorName(product.getVendor() != null ? product.getVendor().getShopName() : null);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setSubCategoryName(product.getSubCategory() != null ? product.getSubCategory().getName() : null);

        // Handle specifications
        if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
            List<ProductSpecificationDTO> specDTOs = product.getSpecifications().stream()
                    .map(spec -> {
                        ProductSpecificationDTO specDTO = new ProductSpecificationDTO();
                        specDTO.setKey(spec.getKey());
                        specDTO.setValue(spec.getValue());
                        specDTO.setDisplayOrder(spec.getDisplayOrder());
                        return specDTO;
                    })
                    .collect(Collectors.toList());
            dto.setSpecifications(specDTOs);
        } else {
            dto.setSpecifications(new ArrayList<>());
        }

        return dto;
    }

    private FileDataDTO mapFileDataToDTO(FileData fileData) {
        return FileDataDTO.builder()
                .id(fileData.getId())
                .fileName(fileData.getFileName())
                .filePath(fileData.getFilePath())
                .fileType(fileData.getFileType())
                .fileSize(fileData.getFileSize())
                .altText(fileData.getAltText())
                .sortOrder(fileData.getSortOrder())
                .isPrimary(fileData.getIsPrimary())
                .mimeType(fileData.getMimeType())
                .build();
    }

    // ==================== VENDOR PRODUCT MANAGEMENT ====================

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto, Long vendorId) {
        try {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

            Vendor vendor = vendorRepo.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

            if (vendor.getVendorStatus() != VendorStatus.ACTIVE) {
                throw new OperationFailedException("Create product", "Vendor account is not active");
            }

            Product product = toEntity(dto, category, vendor);
            Product savedProduct = productRepo.save(product);
            return toDto(savedProduct);
        } catch (Exception ex) {
            throw new OperationFailedException("Create product", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> getProductsByVendor(Long vendorId) {
        try {
            List<Product> vendorProducts = productRepo.findByVendorId(vendorId);
            return vendorProducts.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve vendor products", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> getProductsByVendor(Long vendorId, Pageable pageable) {
        try {
            Page<Product> vendorProducts = productRepo.findByVendorId(vendorId, pageable);
            return vendorProducts.map(this::toDto);
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve vendor products", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> getActiveProductsByVendor(Long vendorId) {
        try {
            List<Product> products = productRepo.findByVendorIdAndStatus(vendorId, ProductStatus.ACTIVE);
            return products.stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve active vendor products", ex.getMessage());
        }
    }

    public Long getProductCountByVendor(Long vendorId) {
        try {
            return productRepo.countByVendorId(vendorId);
        } catch (Exception ex) {
            throw new OperationFailedException("Get vendor product count", ex.getMessage());
        }
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto, Long vendorId) {
        try {
            Product existing = productRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

            if (!existing.getVendor().getId().equals(vendorId)) {
                throw new UnauthorizedAccessException("update this product");
            }

            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

            // Update basic fields
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setPrice(dto.getPrice());
            existing.setStock(dto.getStock());
            existing.setCategory(category);
            existing.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : existing.getDiscount());
            existing.setBrand(dto.getBrand() != null ? dto.getBrand() : existing.getBrand());

            // Update sub-category
            if (dto.getSubCategoryId() != null) {
                SubCategory subCategory = subCategoryRepo.findById(dto.getSubCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", dto.getSubCategoryId()));
                if (!subCategory.getCategory().getId().equals(category.getId())) {
                    throw new OperationFailedException("Update product", "Sub-category does not belong to the selected category");
                }
                existing.setSubCategory(subCategory);
            } else {
                existing.setSubCategory(null);
            }

            // Update specifications
            specificationRepo.deleteByProductId(id);
            existing.getSpecifications().clear();
            if (dto.getSpecifications() != null && !dto.getSpecifications().isEmpty()) {
                for (ProductSpecificationDTO specDTO : dto.getSpecifications()) {
                    ProductSpecification specification = new ProductSpecification();
                    specification.setKey(specDTO.getKey());
                    specification.setValue(specDTO.getValue());
                    specification.setDisplayOrder(specDTO.getDisplayOrder() != null ? specDTO.getDisplayOrder() : 0);
                    specification.setProduct(existing);
                    existing.getSpecifications().add(specification);
                }
            }

            Product updated = productRepo.save(existing);
            return toDto(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Update product", ex.getMessage());
        }
    }

    @Transactional
    public void deleteProduct(Long id, Long vendorId) {
        try {
            Product product = productRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

            if (!product.getVendor().getId().equals(vendorId)) {
                throw new UnauthorizedAccessException("delete this product");
            }

            productRepo.delete(product);
        } catch (Exception ex) {
            throw new OperationFailedException("Delete product", ex.getMessage());
        }
    }

    // ==================== PUBLIC PRODUCT ENDPOINTS ====================

    public ProductResponseDTO getProductById(Long id) {
        try {
            Product product = productRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

            // Increment view count
            productRepo.incrementViewCount(id);

            return toDto(product);
        } catch (Exception ex) {
            throw new OperationFailedException("Get product by ID", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> getAllProducts() {
        try {
            List<Product> products = productRepo.findAll();
            return products.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve all products", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        try {
            Page<Product> products = productRepo.findAll(pageable);
            return products.map(this::toDto);
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve all products paginated", ex.getMessage());
        }
    }

    // ==================== CATEGORY & STATUS ENDPOINTS ====================

    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        try {
            List<Product> categoryProducts = productRepo.findByCategoryId(categoryId);
            return categoryProducts.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve category products", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        try {
            Page<Product> categoryProducts = productRepo.findByCategoryId(categoryId, pageable);
            return categoryProducts.map(this::toDto);
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve category products paginated", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> getProductsByStatus(ProductStatus status) {
        try {
            List<Product> statusProducts = productRepo.findByStatus(status);
            return statusProducts.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve products by status", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> getProductsByStatus(ProductStatus status, Pageable pageable) {
        try {
            Page<Product> statusProducts = productRepo.findByStatus(status, pageable);
            return statusProducts.map(this::toDto);
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve products by status paginated", ex.getMessage());
        }
    }

    // ==================== SEARCH & FILTER ENDPOINTS ====================

    public List<ProductResponseDTO> searchProducts(String keyword) {
        try {
            List<Product> products = productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(
                    keyword, keyword, keyword
            );
            return products.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Search products", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> searchProducts(String keyword, Pageable pageable) {
        try {
            Page<Product> products = productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(
                    keyword, keyword, keyword, pageable
            );
            return products.map(this::toDto);
        } catch (Exception ex) {
            throw new OperationFailedException("Search products paginated", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> filterProducts(Long categoryId, Long subCategoryId,
                                                   Double minPrice, Double maxPrice, String brand) {
        try {
            List<Product> products = productRepo.findByFilters(
                    categoryId, subCategoryId, minPrice, maxPrice, brand, ProductStatus.ACTIVE
            );
            return products.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Filter products", ex.getMessage());
        }
    }

    public Page<ProductResponseDTO> filterProducts(Long categoryId, Long subCategoryId,
                                                   Double minPrice, Double maxPrice, String brand,
                                                   ProductStatus status, Pageable pageable) {
        try {
            // For paginated filtering, we need to implement a custom method or use specification
            // For now, let's get all and paginate manually (not efficient for large datasets)
            List<Product> products = productRepo.findByFilters(
                    categoryId, subCategoryId, minPrice, maxPrice, brand, status
            );

            // Manual pagination (consider implementing proper JPA Specification for better performance)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), products.size());
            List<Product> paginatedProducts = products.subList(start, end);

            List<ProductResponseDTO> dtos = paginatedProducts.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return new org.springframework.data.domain.PageImpl<>(
                    dtos, pageable, products.size()
            );
        } catch (Exception ex) {
            throw new OperationFailedException("Filter products paginated", ex.getMessage());
        }
    }

    // ==================== FEATURED PRODUCTS ENDPOINTS ====================

    public List<ProductResponseDTO> getTrendingProducts(int limit) {
        try {
            List<Product> products = productRepo.findTrendingProducts(limit);
            return products.stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception ex) {
            // Fallback to active products
            return getFallbackProducts(limit);
        }
    }

    public List<ProductResponseDTO> getBestSellingProducts(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Product> products = productRepo.findBestSellingProducts(pageable);
            return products.stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception ex) {
            return getFallbackProducts(limit);
        }
    }

    public List<ProductResponseDTO> getFeaturedProducts(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Product> products = productRepo.findFeaturedProducts(pageable);
            return products.stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception ex) {
            return getFallbackProducts(limit);
        }
    }

    private List<ProductResponseDTO> getFallbackProducts(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Product> products = productRepo.findByStatus(ProductStatus.ACTIVE, pageable).getContent();
            return products.stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Transactional
    public ProductResponseDTO changeProductStatus(Long id, ProductStatus newStatus) {
        try {
            Product product = productRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

            product.setStatus(newStatus);
            Product updated = productRepo.save(product);
            return toDto(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Change product status", ex.getMessage());
        }
    }

    // ==================== ADDITIONAL UTILITY METHODS ====================

    public List<ProductResponseDTO> getProductsBySubCategory(Long subCategoryId) {
        try {
            if (!subCategoryRepo.existsById(subCategoryId)) {
                throw new ResourceNotFoundException("SubCategory", "id", subCategoryId);
            }

            List<Product> allProducts = productRepo.findAll();
            return allProducts.stream()
                    .filter(product -> product.getSubCategory() != null &&
                            product.getSubCategory().getId().equals(subCategoryId))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve sub-category products", ex.getMessage());
        }
    }

    public Long getTotalProductCount() {
        try {
            return productRepo.count();
        } catch (Exception ex) {
            throw new OperationFailedException("Get total product count", ex.getMessage());
        }
    }

    public Long getActiveProductCount() {
        try {
            return productRepo.countByStatus(ProductStatus.ACTIVE);
        } catch (Exception ex) {
            throw new OperationFailedException("Get active product count", ex.getMessage());
        }
    }

    public List<ProductResponseDTO> getFeaturedProductsByCategory(Long categoryId) {
        try {
            List<Product> products = productRepo.findByIsFeaturedTrueAndStatus(ProductStatus.ACTIVE);
            return products.stream()
                    .filter(product -> product.getCategory() != null &&
                            product.getCategory().getId().equals(categoryId))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Get featured products by category", ex.getMessage());
        }
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity) {
        try {
            productRepo.incrementSalesCount(productId, quantity);
        } catch (Exception ex) {
            throw new OperationFailedException("Increment sales count", ex.getMessage());
        }
    }
}
