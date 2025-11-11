package com.exampleOf.EcommerceApplication.service.impl;

import com.exampleOf.EcommerceApplication.dto.VendorDto;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.enums.UserRole;
import com.exampleOf.EcommerceApplication.entity.Vendor;
import com.exampleOf.EcommerceApplication.repository.VendorRepository;
import com.exampleOf.EcommerceApplication.service.UserService;
import com.exampleOf.EcommerceApplication.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final UserService userService;


    @Override
    public Vendor registerVendor(VendorDto vendorDto) {
        // Check if user exists and has VENDOR role
        User user = userService.getUserByEmail(vendorDto.getUserEmail());
        if (user.getRole() != UserRole.VENDOR) {
            throw new RuntimeException("User must have VENDOR role to register as vendor");
        }

        // Check if vendor already exists for this user
        if (vendorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Vendor profile already exists for this user");
        }

        // Check if tax number is unique
        if (vendorDto.getTaxNumber() != null &&
                vendorRepository.existsByTaxNumber(vendorDto.getTaxNumber())) {
            throw new RuntimeException("Tax number already exists: " + vendorDto.getTaxNumber());
        }

        Vendor vendor = new Vendor();
        vendor.setShopName(vendorDto.getShopName());
        vendor.setBusinessDescription(vendorDto.getBusinessDescription());
        vendor.setTaxNumber(vendorDto.getTaxNumber());
        vendor.setAddress(vendorDto.getAddress());
        vendor.setUser(user);
        vendor.setApproved(false);

        return vendorRepository.save(vendor);
    }

    @Override
    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    @Override
    public Vendor getVendorByUserId(Long userId) {
        return vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Vendor not found for user id: " + userId));
    }

    @Override
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Override
    public List<Vendor> getApprovedVendors() {
        return vendorRepository.findByApprovedTrue();
    }

    @Override
    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByApprovedFalse();
    }

    @Override
    public Vendor approveVendor(Long vendorId) {
        Vendor vendor = getVendorById(vendorId);
        vendor.setApproved(true);
        return vendorRepository.save(vendor);
    }

    @Override
    public Vendor updateVendor(Long id, VendorDto vendorDto) {
        Vendor vendor = getVendorById(id);

        if (vendorDto.getShopName() != null) {
            vendor.setShopName(vendorDto.getShopName());
        }
        if (vendorDto.getBusinessDescription() != null) {
            vendor.setBusinessDescription(vendorDto.getBusinessDescription());
        }
        if (vendorDto.getAddress() != null) {
            vendor.setAddress(vendorDto.getAddress());
        }

        return vendorRepository.save(vendor);
    }

    @Override
    public Vendor getVendorByUserEmail(String email) {
        return vendorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Vendor not found for user email: " + email));
    }

    /**
     * Check if a vendor exists by tax number
     */
    public boolean existsByTaxNumber(String taxNumber) {
        return vendorRepository.existsByTaxNumber(taxNumber);
    }

    /**
     * Get vendors by approval status dynamically
     */
    public List<Vendor> getVendorsByApprovalStatus(boolean approved) {
        return approved ? vendorRepository.findByApprovedTrue() : vendorRepository.findByApprovedFalse();
    }

    /**
     * Activate a vendor by setting status ACTIVE and approved true
     */
    public Vendor activateVendor(Long vendorId) {
        Vendor vendor = getVendorById(vendorId);
        vendor.setApproved(true);
        vendor.setVendorStatus(com.exampleOf.EcommerceApplication.enums.VendorStatus.ACTIVE);
        return vendorRepository.save(vendor);
    }

    /**
     * Deactivate a vendor by setting status INACTIVE and approved false
     */
    public Vendor deactivateVendor(Long vendorId) {
        Vendor vendor = getVendorById(vendorId);
        vendor.setApproved(false);
        vendor.setVendorStatus(com.exampleOf.EcommerceApplication.enums.VendorStatus.INACTIVE);
        return vendorRepository.save(vendor);
    }
}