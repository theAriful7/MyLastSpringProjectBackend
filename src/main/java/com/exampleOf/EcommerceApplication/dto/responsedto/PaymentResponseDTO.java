package com.exampleOf.EcommerceApplication.dto.responsedto;


import com.exampleOf.EcommerceApplication.enums.OnlinePaymentType;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import com.exampleOf.EcommerceApplication.enums.PaymentOption;
import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentOption paymentOption;
    private OnlinePaymentType onlinePaymentType;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;

}
