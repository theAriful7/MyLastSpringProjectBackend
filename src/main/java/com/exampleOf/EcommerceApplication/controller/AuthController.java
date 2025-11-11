package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.config.JwtService;
import com.exampleOf.EcommerceApplication.dto.AuthRequest;
import com.exampleOf.EcommerceApplication.dto.AuthResponse;
import com.exampleOf.EcommerceApplication.dto.UserDto;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(user);

        User userDetails = userService.getUserByEmail(request.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .firstName(userDetails.getFirstName())
                .userId(userDetails.getId()) // ✅ Added user ID for frontend
                .message("Login successful")
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDto userDto) {
        User user = userService.registerUser(userDto);
        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .userId(user.getId()) // ✅ Added user ID
                .message("Registration successful")
                .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String username = jwtService.extractUsername(jwt);

            // ✅ Use getUserByEmail instead of loadUserByUsername
            User user = userService.getUserByEmail(username);

            // ✅ Convert User to UserDetails for validation
            UserDetails userDetails = (UserDetails) user;

            return ResponseEntity.ok(jwtService.isTokenValid(jwt, userDetails));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    // ✅ NEW: Refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String username = jwtService.extractUsername(jwt);

            User user = userService.getUserByEmail(username);
            String newToken = jwtService.generateToken(user);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(newToken)
                    .email(user.getEmail())
                    .role(user.getRole())
                    .firstName(user.getFirstName())
                    .userId(user.getId())
                    .message("Token refreshed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .message("Invalid or expired token")
                            .build()
            );
        }
    }
}