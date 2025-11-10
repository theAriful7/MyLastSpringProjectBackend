package com.exampleOf.EcommerceApplication.dto.responsedto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SubCategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
