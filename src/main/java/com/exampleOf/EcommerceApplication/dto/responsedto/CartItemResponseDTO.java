package com.exampleOf.EcommerceApplication.dto.responsedto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage; // ✅ Now this will work!
    private BigDecimal pricePerItem;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Long cartId;
}
