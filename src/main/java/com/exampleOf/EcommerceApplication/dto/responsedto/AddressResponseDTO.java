package com.exampleOf.EcommerceApplication.dto.responsedto;

import lombok.Data;

@Data
public class AddressResponseDTO {

    private String recipientName;
    private Long id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
}
