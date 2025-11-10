package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.Exception.CustomException.AlreadyExistsException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.OperationFailedException;
import com.exampleOf.EcommerceApplication.Exception.CustomException.ResourceNotFoundException;
import com.exampleOf.EcommerceApplication.dto.requestdto.CategoryRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CategoryResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Category;
import com.exampleOf.EcommerceApplication.repository.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepo categoryRepo;

    // ✅ Convert Entity → DTO
    private CategoryResponseDTO toDto(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    // ✅ Convert DTO → Entity
    private Category toEntity(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        return category;
    }

    // ✅ Create Category with duplicate check
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        // Check if category name already exists (case-insensitive)
        boolean exists = categoryRepo.existsByNameIgnoreCase(dto.getName().trim());
        if (exists) {
            throw new AlreadyExistsException("Category '" + dto.getName() + "' already exists!");
        }

        Category category = toEntity(dto);
        Category saved = categoryRepo.save(category);
        return toDto(saved);
    }

    // ✅ Get All Categories
    public List<CategoryResponseDTO> getAllCategories() {
        try {
            return categoryRepo.findAll()
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Retrieve categories", ex.getMessage());
        }
    }

    // ✅ Get Category By ID with proper exception
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return toDto(category);
    }

    // ✅ Update Category with duplicate check and proper validation
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        // Find existing category
        Category existing = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if new name already exists (excluding current category)
        if (dto.getName() != null && !dto.getName().trim().equalsIgnoreCase(existing.getName())) {
            boolean nameExists = categoryRepo.existsByNameIgnoreCase(dto.getName().trim());
            if (nameExists) {
                throw new AlreadyExistsException("Category name '" + dto.getName() + "' already exists!");
            }
            existing.setName(dto.getName().trim());
        }

        // Update description if provided
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim());
        }

        try {
            Category updated = categoryRepo.save(existing);
            return toDto(updated);
        } catch (Exception ex) {
            throw new OperationFailedException("Update category", ex.getMessage());
        }
    }

    // ✅ Delete Category with proper response
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if category has products (optional - for data integrity)
        if (!category.getProducts().isEmpty()) {
            throw new OperationFailedException(
                    "Delete category",
                    "Category has products associated. Please remove products first. Category ID: " + id
            );
        }

        try {
            categoryRepo.delete(category);
        } catch (Exception ex) {
            throw new OperationFailedException("Delete category", ex.getMessage());
        }
    }

    // ✅ Additional useful methods

    // Check if category exists
    public boolean categoryExists(Long id) {
        return categoryRepo.existsById(id);
    }

    // Get category by name (case-insensitive)
    public CategoryResponseDTO getCategoryByName(String name) {
        Category category = categoryRepo.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
        return toDto(category);
    }

    // Search categories by name (partial match)
    public List<CategoryResponseDTO> searchCategories(String keyword) {
        try {
            return categoryRepo.findAll().stream()
                    .filter(category ->
                            category.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                    (category.getDescription() != null &&
                                            category.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                    )
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new OperationFailedException("Search categories", ex.getMessage());
        }
    }
}
