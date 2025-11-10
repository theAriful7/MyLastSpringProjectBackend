package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.requestdto.SubCategoryRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.SubCategoryResponseDTO;
import com.exampleOf.EcommerceApplication.service.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sub-categories")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    // ✅ Create SubCategory - HTTP 201
    @PostMapping
    public ResponseEntity<SubCategoryResponseDTO> createSubCategory(
            @Valid @RequestBody SubCategoryRequestDTO dto) {
        SubCategoryResponseDTO response = subCategoryService.createSubCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Get All SubCategories - HTTP 200
    @GetMapping
    public ResponseEntity<List<SubCategoryResponseDTO>> getAllSubCategories() {
        return ResponseEntity.ok(subCategoryService.getAllSubCategories());
    }

    // ✅ Get SubCategory By ID - HTTP 200
    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryResponseDTO> getSubCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(id));
    }

    // ✅ Get SubCategories By Category ID - HTTP 200
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubCategoryResponseDTO>> getSubCategoriesByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(subCategoryService.getSubCategoriesByCategory(categoryId));
    }

    // ✅ Update SubCategory - HTTP 200
    @PutMapping("/{id}")
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody SubCategoryRequestDTO dto) {
        return ResponseEntity.ok(subCategoryService.updateSubCategory(id, dto));
    }

    // ✅ Delete SubCategory - HTTP 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long id) {
        subCategoryService.deleteSubCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Search SubCategories - HTTP 200
    @GetMapping("/search")
    public ResponseEntity<List<SubCategoryResponseDTO>> searchSubCategories(
            @RequestParam String keyword) {
        return ResponseEntity.ok(subCategoryService.searchSubCategories(keyword));
    }
}
