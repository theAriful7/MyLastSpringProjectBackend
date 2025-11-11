package com.exampleOf.EcommerceApplication.entity;


import com.exampleOf.EcommerceApplication.enums.OnlinePaymentType;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import com.exampleOf.EcommerceApplication.enums.PaymentOption;
import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@EqualsAndHashCode(callSuper = true)
public class Payment extends Base{

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // ✅ Changed from String → Enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentOption paymentOption;

    // ✅ Only used when paymentMethod == ONLINE_PAYMENT
    @Enumerated(EnumType.STRING)
    private OnlinePaymentType onlinePaymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_id")
    private String transactionId; // For online payment reference

    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null && paymentStatus == PaymentStatus.COMPLETED) {
            paymentDate = LocalDateTime.now();
        }
    }
}
