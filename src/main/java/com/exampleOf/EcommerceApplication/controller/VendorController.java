package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.VendorDto;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.entity.Vendor;
import com.exampleOf.EcommerceApplication.service.UserService;
import com.exampleOf.EcommerceApplication.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final UserService userService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Vendor> registerVendor(@RequestBody VendorDto vendorDto) {
        User currentUser = userService.getCurrentUser();
        vendorDto.setUserEmail(currentUser.getEmail());

        Vendor vendor = vendorService.registerVendor(vendorDto);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Vendor> getMyVendorProfile() {
        User currentUser = userService.getCurrentUser();
        Vendor vendor = vendorService.getVendorByUserId(currentUser.getId());
        return ResponseEntity.ok(vendor);
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Vendor> updateMyVendorProfile(@RequestBody VendorDto vendorDto) {
        User currentUser = userService.getCurrentUser();
        Vendor vendor = vendorService.getVendorByUserId(currentUser.getId());
        Vendor updatedVendor = vendorService.updateVendor(vendor.getId(), vendorDto);
        return ResponseEntity.ok(updatedVendor);
    }

    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        List<Vendor> vendors = vendorService.getApprovedVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Vendor>> getPendingVendors() {
        List<Vendor> vendors = vendorService.getPendingVendors();
        return ResponseEntity.ok(vendors);
    }

    @PutMapping("/{vendorId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vendor> approveVendor(@PathVariable Long vendorId) {
        Vendor vendor = vendorService.approveVendor(vendorId);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Long id) {
        Vendor vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(vendor);
    }
}