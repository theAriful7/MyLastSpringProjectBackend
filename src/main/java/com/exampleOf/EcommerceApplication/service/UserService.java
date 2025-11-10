package com.exampleOf.EcommerceApplication.service;


import com.exampleOf.EcommerceApplication.dto.UserDto;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.enums.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User registerUser(UserDto userDto);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getUsersByRole(UserRole role);
    User updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    User getCurrentUser();
}