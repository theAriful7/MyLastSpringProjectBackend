package com.exampleOf.EcommerceApplication.dto;

import com.exampleOf.EcommerceApplication.enums.VendorStatus;
import lombok.Data;

@Data
public class VendorDto {
    private String shopName;
    private String businessDescription;
    private String taxNumber;
    private VendorStatus vendorStatus;

    private String address;
    private String userEmail; // To associate with user
}
