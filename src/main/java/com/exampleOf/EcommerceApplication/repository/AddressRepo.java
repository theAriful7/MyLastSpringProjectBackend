package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Address;
import com.exampleOf.EcommerceApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepo extends JpaRepository<Address, Long> {
    // Find all addresses by user
    List<Address> findByUser(User user);

    // Find specific address by ID and user (for security)
    Optional<Address> findByIdAndUser(Long id, User user);

    // Check if address exists for user
    boolean existsByUser(User user);

    // Delete all addresses by user
    void deleteAllByUser(User user);

    // Count addresses by user (if you want to add custom query)
    // @Query("SELECT COUNT(a) FROM Address a WHERE a.user = :user")
    // int countByUser(@Param("user") User user);
}
