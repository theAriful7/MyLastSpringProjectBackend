package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

@Data
public class ProductSpecificationDTO {
    private String key;     // Example: "color"
    private String value;   // Example: "Red"
    private Integer displayOrder; // Example: 1 (for sorting)
}
