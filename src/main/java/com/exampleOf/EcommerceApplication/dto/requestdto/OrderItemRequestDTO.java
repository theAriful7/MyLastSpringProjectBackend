package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Long productId;
    private Integer quantity;
}
