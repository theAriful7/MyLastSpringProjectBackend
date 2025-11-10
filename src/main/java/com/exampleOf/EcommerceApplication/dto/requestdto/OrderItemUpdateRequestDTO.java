package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

@Data
public class OrderItemUpdateRequestDTO {
    private Long productId;
    private Integer quantity;
}
