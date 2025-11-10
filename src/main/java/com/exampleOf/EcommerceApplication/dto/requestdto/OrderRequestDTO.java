package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequestDTO {
    private Long userId;
    private Long shippingAddressId;
    private List<OrderItemRequestDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
