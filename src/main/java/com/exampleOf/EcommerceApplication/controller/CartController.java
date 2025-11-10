package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.requestdto.CartRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CartResponseDTO;
import com.exampleOf.EcommerceApplication.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
//@CrossOrigin(origins = "http://localhost:4200")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ✅ NEW: Get cart by User ID (Most used endpoint!)
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    // ✅ NEW: Clear cart items
    @PostMapping("/{cartId}/clear")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.clearCart(cartId));
    }

    // ⚠️ WARNING: This can create duplicate carts!
    @PostMapping
    public ResponseEntity<CartResponseDTO> createCart(@RequestBody CartRequestDTO dto) {
        return ResponseEntity.ok(cartService.createCart(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponseDTO> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @GetMapping
    public ResponseEntity<List<CartResponseDTO>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCart(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.deleteCart(id));
    }
}
