package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.dto.VendorDto;
import com.exampleOf.EcommerceApplication.entity.Vendor;

import java.util.List;

public interface VendorService {
    Vendor registerVendor(VendorDto vendorDto);
    Vendor getVendorById(Long id);
    Vendor getVendorByUserId(Long userId);
    List<Vendor> getAllVendors();
    List<Vendor> getApprovedVendors();
    List<Vendor> getPendingVendors();
    Vendor approveVendor(Long vendorId);
    Vendor updateVendor(Long id, VendorDto vendorDto);

    Vendor getVendorByUserEmail(String email); // Add this method

}