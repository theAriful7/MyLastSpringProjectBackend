package com.exampleOf.EcommerceApplication.dto;

import lombok.Data;

@Data
public class VendorDto {
    private String shopName;
    private String businessDescription;
    private String taxNumber;
    private String address;
    private String userEmail; // To associate with user
}
