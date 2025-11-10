package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.UserDto;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.enums.UserRole;
import com.exampleOf.EcommerceApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateCurrentUser(@RequestBody UserDto userDto) {
        User currentUser = userService.getCurrentUser();
        User updatedUser = userService.updateUser(currentUser.getId(), userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        // Convert string to enum
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        List<User> users = userService.getUsersByRole(userRole);
        return ResponseEntity.ok(users);
    }
}