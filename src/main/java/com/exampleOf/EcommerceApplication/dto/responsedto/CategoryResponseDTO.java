package com.exampleOf.EcommerceApplication.dto.responsedto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;   // optional
    private LocalDateTime updatedAt;
}
