package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);
    Optional<Vendor> findByUserEmail(String email);
    List<Vendor> findByApprovedTrue();
    List<Vendor> findByApprovedFalse();
    boolean existsByTaxNumber(String taxNumber);
}