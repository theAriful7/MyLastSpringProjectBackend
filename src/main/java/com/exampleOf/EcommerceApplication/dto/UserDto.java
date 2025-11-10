package com.exampleOf.EcommerceApplication.dto;

import com.exampleOf.EcommerceApplication.enums.UserRole;
import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role = UserRole.CUSTOMER;
}