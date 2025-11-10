package com.exampleOf.EcommerceApplication.controller;

import com.exampleOf.EcommerceApplication.dto.requestdto.CartItemRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CartItemResponseDTO;
import com.exampleOf.EcommerceApplication.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart_items")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    // ✅ CREATE: Add item to cart
    @PostMapping
    public ResponseEntity<CartItemResponseDTO> createCartItem(@RequestBody CartItemRequestDTO requestDTO) {
        CartItemResponseDTO response = cartItemService.addItemToCart(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ✅ READ: Get cart item by ID
    @GetMapping("/{id}")
    public ResponseEntity<CartItemResponseDTO> getCartItemById(@PathVariable Long id) {
        CartItemResponseDTO response = cartItemService.getCartItemById(id);
        return ResponseEntity.ok(response);
    }

    // ✅ READ: Get all cart items for a specific cart
    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemResponseDTO>> getCartItemsByCartId(@PathVariable Long cartId) {
        List<CartItemResponseDTO> responses = cartItemService.getCartItemsByCartId(cartId);
        return ResponseEntity.ok(responses);
    }

    // ✅ READ: Get all cart items (Admin only)
    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getAllCartItems() {
        List<CartItemResponseDTO> responses = cartItemService.getAllCartItems();
        return ResponseEntity.ok(responses);
    }

    // ✅ READ: Get specific cart item by cart and product
    @GetMapping("/cart/{cartId}/product/{productId}")
    public ResponseEntity<CartItemResponseDTO> getCartItemByCartAndProduct(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        CartItemResponseDTO response = cartItemService.getCartItemByCartAndProduct(cartId, productId);
        return ResponseEntity.ok(response);
    }

    // ✅ READ: Check if product exists in cart
    @GetMapping("/cart/{cartId}/product/{productId}/exists")
    public ResponseEntity<Boolean> isProductInCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        boolean exists = cartItemService.isProductInCart(cartId, productId);
        return ResponseEntity.ok(exists);
    }

    // ✅ READ: Get cart items count
    @GetMapping("/cart/{cartId}/count")
    public ResponseEntity<Integer> getCartItemsCount(@PathVariable Long cartId) {
        Integer count = cartItemService.getCartItemsCount(cartId);
        return ResponseEntity.ok(count);
    }

    // ✅ READ: Get cart subtotal
    @GetMapping("/cart/{cartId}/subtotal")
    public ResponseEntity<Double> getCartSubtotal(@PathVariable Long cartId) {
        Double subtotal = cartItemService.getCartSubtotal(cartId);
        return ResponseEntity.ok(subtotal);
    }

    // ✅ UPDATE: Update cart item quantity only
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<CartItemResponseDTO> updateCartItemQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        CartItemResponseDTO response = cartItemService.updateCartItemQuantity(id, quantity);
        return ResponseEntity.ok(response);
    }

    // ✅ UPDATE: Update cart item (full update)
    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @PathVariable Long id,
            @RequestBody CartItemRequestDTO requestDTO) {
        CartItemResponseDTO response = cartItemService.updateCartItem(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id) {
        if (!cartItemService.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cart item with ID " + id + " not found!");
        }

        cartItemService.deleteCartItem(id);
        return ResponseEntity.ok("✅ Cart item deleted successfully!");
    }

    // ✅ DELETE: Remove cart item by cart and product
    @DeleteMapping("/cart/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteCartItemByCartAndProduct(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        String message = cartItemService.deleteCartItemByCartAndProduct(cartId, productId);
        return ResponseEntity.ok(message);
    }

    // ✅ DELETE: Remove all items from cart
    @DeleteMapping("/cart/{cartId}/clear")
    public ResponseEntity<String> clearCartItems(@PathVariable Long cartId) {
        String message = cartItemService.clearCartItems(cartId);
        return ResponseEntity.ok(message);
    }
}
