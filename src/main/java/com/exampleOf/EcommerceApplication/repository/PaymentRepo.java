package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
}
