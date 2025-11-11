package com.exampleOf.EcommerceApplication.dto;

import com.exampleOf.EcommerceApplication.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long userId;
    private String token;
    private String email;
    private UserRole role;
    private String firstName;
    private String message;
}