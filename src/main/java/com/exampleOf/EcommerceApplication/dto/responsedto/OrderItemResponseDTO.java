package com.exampleOf.EcommerceApplication.dto.responsedto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id; // Added ID field
    private Long productId;
    private String productName;
    private String productImage; // Added for frontend display
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
