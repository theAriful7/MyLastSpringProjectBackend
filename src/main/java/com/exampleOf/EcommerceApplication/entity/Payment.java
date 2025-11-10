package com.exampleOf.EcommerceApplication.entity;


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

    @Column(nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_id")
    private String transactionId; // For payment gateway reference

    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null && paymentStatus == PaymentStatus.COMPLETED) {
            paymentDate = LocalDateTime.now();
        }
    }
}
