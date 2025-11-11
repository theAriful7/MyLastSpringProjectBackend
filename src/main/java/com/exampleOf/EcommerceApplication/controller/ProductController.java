package com.exampleOf.EcommerceApplication.controller;


import com.exampleOf.EcommerceApplication.dto.requestdto.ProductRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.ProductResponseDTO;
import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import com.exampleOf.EcommerceApplication.service.ProductService;
import com.exampleOf.EcommerceApplication.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final VendorService vendorService;

    // ✅ VENDOR ENDPOINTS - Secure with vendor ownership check
    @PostMapping("/vendors/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id)")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @PathVariable Long vendorId,
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO product = productService.createProduct(productRequestDTO, vendorId);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping("/vendors/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getVendorProducts(@PathVariable Long vendorId) {
        List<ProductResponseDTO> products = productService.getProductsByVendor(vendorId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/vendors/{vendorId}/paginated")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponseDTO>> getVendorProductsPaginated(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getProductsByVendor(vendorId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/vendors/{vendorId}/active")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getActiveVendorProducts(@PathVariable Long vendorId) {
        List<ProductResponseDTO> products = productService.getActiveProductsByVendor(vendorId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/vendors/{vendorId}/count")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Long> getVendorProductCount(@PathVariable Long vendorId) {
        Long count = productService.getProductCountByVendor(vendorId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{productId}/vendors/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long productId,
            @PathVariable Long vendorId,
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO product = productService.updateProduct(productId, productRequestDTO, vendorId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}/vendors/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') and @vendorService.isVendorOwner(#vendorId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @PathVariable Long vendorId) {
        productService.deleteProduct(productId, vendorId);
        return ResponseEntity.noContent().build();
    }

    // ✅ PUBLIC PRODUCT ENDPOINTS
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long productId) {
        ProductResponseDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    // ✅ CATEGORY & STATUS ENDPOINTS
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}/paginated")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategoryPaginated(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByStatus(@PathVariable ProductStatus status) {
        List<ProductResponseDTO> products = productService.getProductsByStatus(status);
        return ResponseEntity.ok(products);
    }

    // ✅ SEARCH & FILTER ENDPOINTS
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductResponseDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<ProductResponseDTO>> searchProductsPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    // ✅ UPDATED: Filter endpoint with pagination
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponseDTO>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponseDTO> products = productService.filterProducts(
                categoryId, subCategoryId, minPrice, maxPrice,
                brand, minRating, inStock, pageable);

        return ResponseEntity.ok(products);
    }

    // ✅ FEATURED PRODUCTS ENDPOINTS
    @GetMapping("/trending")
    public ResponseEntity<List<ProductResponseDTO>> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponseDTO> products = productService.getTrendingProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductResponseDTO>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponseDTO> products = productService.getBestSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponseDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponseDTO> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }

    // ✅ ADMIN ENDPOINTS
    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> changeProductStatus(
            @PathVariable Long productId,
            @RequestParam ProductStatus newStatus) {
        ProductResponseDTO product = productService.changeProductStatus(productId, newStatus);
        return ResponseEntity.ok(product);
    }

    // ✅ NEW: Get similar products
    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductResponseDTO>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductResponseDTO> products = productService.getSimilarProducts(productId, limit);
        return ResponseEntity.ok(products);
    }

    // ✅ NEW: Get price range for filters
    @GetMapping("/price-range")
    public ResponseEntity<Object[]> getPriceRange(@RequestParam(required = false) Long categoryId) {
        Object[] priceRange = productService.getPriceRange(categoryId);
        return ResponseEntity.ok(priceRange);
    }

    // ✅ NEW: Get available brands for filters
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAvailableBrands(@RequestParam(required = false) Long categoryId) {
        List<String> brands = productService.getAvailableBrands(categoryId);
        return ResponseEntity.ok(brands);
    }

    // ✅ NEW: Get products by sub-category
    @GetMapping("/sub-category/{subCategoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsBySubCategory(@PathVariable Long subCategoryId) {
        List<ProductResponseDTO> products = productService.getProductsBySubCategory(subCategoryId);
        return ResponseEntity.ok(products);
    }

    // ✅ NEW: Statistics
    @GetMapping("/statistics/count")
    public ResponseEntity<Long> getTotalProductCount() {
        Long count = productService.getTotalProductCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/active-count")
    public ResponseEntity<Long> getActiveProductCount() {
        Long count = productService.getActiveProductCount();
        return ResponseEntity.ok(count);
    }

}
