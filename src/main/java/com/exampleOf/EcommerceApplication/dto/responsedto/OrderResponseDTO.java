package com.exampleOf.EcommerceApplication.dto.responsedto;


import com.exampleOf.EcommerceApplication.entity.Address;
import com.exampleOf.EcommerceApplication.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private Long userId;



    private BigDecimal totalAmount; // Double → BigDecimal for money accuracy

    private OrderStatus status;
    private LocalDateTime orderDate;
    private Address shippingAddress;

    private List<OrderItemResponseDTO> items;
}
