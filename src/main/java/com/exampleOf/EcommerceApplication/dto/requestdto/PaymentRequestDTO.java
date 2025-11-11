package com.exampleOf.EcommerceApplication.dto.requestdto;

import com.exampleOf.EcommerceApplication.enums.OnlinePaymentType;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import com.exampleOf.EcommerceApplication.enums.PaymentOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private PaymentOption paymentOption;

    private OnlinePaymentType onlinePaymentType;

    private String transactionId;

}
