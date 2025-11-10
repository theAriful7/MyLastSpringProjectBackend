package com.exampleOf.EcommerceApplication.dto.responsedto;


import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {

    private Long id;                      // Payment এর unique ID
    private Long orderId;                 // Payment কোন Order এর জন্য
    private BigDecimal amount;            // Payment amount
    private String paymentMethod;         // Payment method
    private PaymentStatus paymentStatus;  // Enum type
    private LocalDateTime paymentDate;

}
