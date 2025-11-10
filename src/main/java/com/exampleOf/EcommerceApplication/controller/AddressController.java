package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.requestdto.AddressRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.AddressResponseDTO;
import com.exampleOf.EcommerceApplication.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // ✅ Get all addresses
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    // ✅ Get address by ID
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    // ✅ Get addresses by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }

    // ✅ Create a new address
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.createAddress(dto));
    }

    // ✅ Update address by ID
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable Long id,
                                                            @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.updateAddress(id, dto));
    }

    // ❌ Delete address by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.deleteAddress(id));
    }

    // ❌ Delete address with user validation
    @DeleteMapping("/{addressId}/user/{userId}")
    public ResponseEntity<String> deleteAddressWithUser(@PathVariable Long addressId,
                                                        @PathVariable Long userId) {
        return ResponseEntity.ok(addressService.deleteAddress(addressId, userId));
    }

    // ✅ Get count of addresses for a user
    @GetMapping("/count/{userId}")
    public ResponseEntity<Integer> getAddressCountByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getAddressCountByUser(userId));
    }

    // ✅ Check if user has any addresses
    @GetMapping("/has/{userId}")
    public ResponseEntity<Boolean> userHasAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.userHasAddresses(userId));
    }

    // ✅ Get default address for a user
    @GetMapping("/default/{userId}")
    public ResponseEntity<AddressResponseDTO> getDefaultAddress(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getDefaultAddress(userId));
    }
}

