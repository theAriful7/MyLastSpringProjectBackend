package com.exampleOf.EcommerceApplication.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotNull(message = "User ID is required")
    private Long user_id;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private String country;

    private String postalCode;

    @NotBlank(message = "Phone is required")
    private String phone;
}
