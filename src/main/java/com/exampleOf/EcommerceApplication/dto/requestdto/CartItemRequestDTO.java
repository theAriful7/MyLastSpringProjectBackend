package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

@Data
public class CartItemRequestDTO {
    private Long cartId;        // Required: Which cart to add to
    private Long productId;     // Required: Which product to add
    private Integer quantity = 1;
//    private Long userId;
}
