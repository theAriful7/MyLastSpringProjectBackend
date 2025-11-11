package com.exampleOf.EcommerceApplication.dto.requestdto;

import com.exampleOf.EcommerceApplication.enums.OnlinePaymentType;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private OnlinePaymentType onlinePaymentType;

    private String transactionId;

}
