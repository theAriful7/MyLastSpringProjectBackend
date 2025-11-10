package com.exampleOf.EcommerceApplication.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubCategoryRequestDTO {

    @NotBlank(message = "Sub-category name is required")
    @Size(max = 100, message = "Sub-category name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
