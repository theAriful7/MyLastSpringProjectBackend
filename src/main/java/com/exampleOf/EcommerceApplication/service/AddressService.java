package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.dto.requestdto.AddressRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.AddressResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Address;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.repository.AddressRepo;
import com.exampleOf.EcommerceApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepo addressRepo;
    private final UserRepository userRepository;

    // ✅ GET ADDRESS BY ID
    public AddressResponseDTO getAddressById(Long id) {
        Address address = addressRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));
        return toDto(address);
    }

    // ✅ GET ALL ADDRESSES
    public List<AddressResponseDTO> getAllAddresses() {
        return addressRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ➕ CREATE ADDRESS
    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        User user = userRepository.findById(dto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUser_id()));

        Address address = toEntity(dto, user);
        Address saved = addressRepo.save(address);
        return toDto(saved);
    }

    // ✏️ UPDATE ADDRESS
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto) {
        Address existing = addressRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));

        // Update ALL fields
        existing.setRecipientName(dto.getRecipientName());
        existing.setStreet(dto.getStreet());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        existing.setCountry(dto.getCountry());
        existing.setPostalCode(dto.getPostalCode());
        existing.setPhone(dto.getPhone());

        Address updated = addressRepo.save(existing);
        return toDto(updated);
    }


    public List<AddressResponseDTO> getAddressesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return addressRepo.findByUser(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ❌ DELETE ADDRESS (Simple version)
    public String deleteAddress(Long id) {
        Address existing = addressRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));
        addressRepo.delete(existing);
        return "Address deleted successfully.";
    }

    // 🔒 DELETE ADDRESS WITH USER VALIDATION (Fixed method name)
    public String deleteAddress(Long addressId, Long userId) {
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Security check: user can only delete their own addresses
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this address");
        }

        addressRepo.delete(address);
        return "Address deleted successfully.";
    }

    // ✅ COUNT: Get total addresses count for user
    public Integer getAddressCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return addressRepo.findByUser(user).size(); // Using list size since countByUser doesn't exist
    }

    // ✅ CHECK: If user has any addresses
    public Boolean userHasAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return !addressRepo.findByUser(user).isEmpty();
    }

    // ✅ GET DEFAULT ADDRESS
    public AddressResponseDTO getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<Address> addresses = addressRepo.findByUser(user);
        if (addresses.isEmpty()) {
            throw new RuntimeException("No addresses found for user");
        }
        return toDto(addresses.get(0));
    }

    // ✅ CONVERT: DTO to Entity with User
    private Address toEntity(AddressRequestDTO dto, User user) {
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(dto.getRecipientName());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setPhone(dto.getPhone());
        return address;
    }

    // ✅ CONVERT: Entity to DTO
    private AddressResponseDTO toDto(Address entity) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(entity.getId());
        dto.setRecipientName(entity.getRecipientName());
        dto.setStreet(entity.getStreet());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        dto.setPhone(entity.getPhone());
        return dto;
    }
}
