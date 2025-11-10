package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private List<FileDataDTO> images;
    private Long categoryId;
    private Long subCategoryId;
    private Double discount;
    private String brand;
    // private Long vendorId; // optional, admin only
    private List<ProductSpecificationDTO> specifications;
}
