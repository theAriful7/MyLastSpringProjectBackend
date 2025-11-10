package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {
    // Find all items in a cart
    List<CartItem> findByCartId(Long cartId);

    // Find specific item by cart and product
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // Delete all items from a cart
    void deleteAllByCartId(Long cartId);

    // Delete specific item by cart and product
    void deleteByCartIdAndProductId(Long cartId, Long productId);

    // Count items in cart
    long countByCartId(Long cartId);

    // Check if product exists in cart
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    // Update quantity for specific item
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.id = :id")
    void updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}
