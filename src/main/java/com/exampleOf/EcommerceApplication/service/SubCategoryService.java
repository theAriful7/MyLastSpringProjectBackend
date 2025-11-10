package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.Exception.CustomException.AlreadyExistsException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.dto.requestdto.SubCategoryRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.SubCategoryResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Category;
import com.exampleOf.EcommerceApplication.entity.SubCategory;
import com.exampleOf.EcommerceApplication.repository.CategoryRepo;
import com.exampleOf.EcommerceApplication.repository.SubCategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubCategoryService {
    private final SubCategoryRepo subCategoryRepo;
    private final CategoryRepo categoryRepo;

    // ✅ Convert Entity → DTO
    private SubCategoryResponseDTO toDto(SubCategory subCategory) {
        SubCategoryResponseDTO dto = new SubCategoryResponseDTO();
        dto.setId(subCategory.getId());
        dto.setName(subCategory.getName());
        dto.setDescription(subCategory.getDescription());
        dto.setCategoryId(subCategory.getCategory().getId());
        dto.setCategoryName(subCategory.getCategory().getName());
        dto.setCreatedAt(subCategory.getCreatedAt());
        dto.setUpdatedAt(subCategory.getUpdatedAt());
        return dto;
    }

    // ✅ Convert DTO → Entity
    private SubCategory toEntity(SubCategoryRequestDTO dto, Category category) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(dto.getName().trim());
        subCategory.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        subCategory.setCategory(category);
        return subCategory;
    }

    // ✅ Create SubCategory
    @Transactional
    public SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO dto) {
        // Find parent category
        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        // Check if sub-category name already exists in this category
        List<SubCategory> existingSubCategories = subCategoryRepo.findByCategoryIdAndName(
                dto.getCategoryId(), dto.getName().trim()
        );

        if (!existingSubCategories.isEmpty()) {
            throw new AlreadyExistsException(
                    "Sub-category '" + dto.getName() + "' already exists in category '" + category.getName() + "'!"
            );
        }

        SubCategory subCategory = toEntity(dto, category);
        SubCategory saved = subCategoryRepo.save(subCategory);
        return toDto(saved);
    }

    // ✅ Get All SubCategories
    public List<SubCategoryResponseDTO> getAllSubCategories() {
        try {
            return subCategoryRepo.findAll()
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve sub-categories", ex.getMessage());
        }
    }

    // ✅ Get SubCategory By ID
    public SubCategoryResponseDTO getSubCategoryById(Long id) {
        SubCategory subCategory = subCategoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id));
        return toDto(subCategory);
    }

    // ✅ Get SubCategories By Category ID
    public List<SubCategoryResponseDTO> getSubCategoriesByCategory(Long categoryId) {
        // Verify category exists
        if (!categoryRepo.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        List<SubCategory> subCategories = subCategoryRepo.findByCategoryId(categoryId);
        return subCategories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Update SubCategory
    @Transactional
    public SubCategoryResponseDTO updateSubCategory(Long id, SubCategoryRequestDTO dto) {
        // Find existing sub-category
        SubCategory existing = subCategoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id));

        // Find parent category
        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        // Check if new name already exists in this category (excluding current sub-category)
        if (dto.getName() != null && !dto.getName().trim().equalsIgnoreCase(existing.getName())) {
            List<SubCategory> existingWithSameName = subCategoryRepo.findByCategoryIdAndName(
                    dto.getCategoryId(), dto.getName().trim()
            );

            // Filter out current sub-category from the list
            boolean nameExists = existingWithSameName.stream()
                    .anyMatch(subCat -> !subCat.getId().equals(id));

            if (nameExists) {
                throw new AlreadyExistsException(
                        "Sub-category name '" + dto.getName() + "' already exists in category '" + category.getName() + "'!"
                );
            }
            existing.setName(dto.getName().trim());
        }

        // Update description if provided
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim());
        }

        // Update category if changed
        if (!existing.getCategory().getId().equals(dto.getCategoryId())) {
            existing.setCategory(category);
        }

        try {
            SubCategory updated = subCategoryRepo.save(existing);
            return toDto(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Update sub-category", ex.getMessage());
        }
    }

    // ✅ Delete SubCategory
    @Transactional
    public void deleteSubCategory(Long id) {
        SubCategory subCategory = subCategoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id));

        // Check if sub-category has products
        if (!subCategory.getProducts().isEmpty()) {
            throw new OperationFailedException(
                    "Delete sub-category",
                    "Sub-category has products associated. Please remove products first. SubCategory ID: " + id
            );
        }

        try {
            subCategoryRepo.delete(subCategory);
        } catch (Exception ex) {
            throw new OperationFailedException("Delete sub-category", ex.getMessage());
        }
    }

    // ✅ Check if sub-category exists
    public boolean subCategoryExists(Long id) {
        return subCategoryRepo.existsById(id);
    }

    // ✅ Search sub-categories by name (partial match)
    public List<SubCategoryResponseDTO> searchSubCategories(String keyword) {
        try {
            return subCategoryRepo.findAll().stream()
                    .filter(subCategory ->
                            subCategory.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                    (subCategory.getDescription() != null &&
                                            subCategory.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                                    subCategory.getCategory().getName().toLowerCase().contains(keyword.toLowerCase())
                    )
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Search sub-categories", ex.getMessage());
        }
    }
}
