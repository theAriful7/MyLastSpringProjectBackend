package com.exampleOf.EcommerceApplication.dto.responsedto;


import com.exampleOf.EcommerceApplication.dto.requestdto.FileDataDTO;
import com.exampleOf.EcommerceApplication.dto.requestdto.ProductSpecificationDTO;
import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private List<FileDataDTO> images;
    private Double discount;
    private String brand;
    private String categoryName;
    private String subCategoryName;
    private ProductStatus status;
    private Long vendorId;
    private String vendorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductSpecificationDTO> specifications;


}
