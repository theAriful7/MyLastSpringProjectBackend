package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Order;
import com.exampleOf.EcommerceApplication.entity.Payment;
import com.exampleOf.EcommerceApplication.enums.PaymentMethod;
import com.exampleOf.EcommerceApplication.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    Optional<Payment> findByTransactionId(String transactionId);
}
