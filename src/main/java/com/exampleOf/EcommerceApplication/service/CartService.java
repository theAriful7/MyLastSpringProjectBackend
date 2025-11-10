package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.dto.requestdto.CartItemRequestDTO;
import com.exampleOf.EcommerceApplication.dto.requestdto.CartRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CartItemResponseDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CartResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Cart;
import com.exampleOf.EcommerceApplication.entity.Product;
import com.exampleOf.EcommerceApplication.entity.User;
import com.exampleOf.EcommerceApplication.repository.CartRepo;
import com.exampleOf.EcommerceApplication.repository.ProductRepo;
import com.exampleOf.EcommerceApplication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepo cartRepo;
    private final UserRepository userRepo;
    private final CartItemService cartItemService;
    private final ProductRepo productRepo;



    // ✅ FIXED: Get or Create Cart for User (Prevents duplicates)
    public Cart getOrCreateCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepo.save(newCart);
                });
    }

    // ✅ FIXED: Create Cart with duplicate check
    public CartResponseDTO createCart(CartRequestDTO dto) {
        // Check if user already has a cart
        if (cartRepo.findByUserId(dto.getUserId()).isPresent()) {
            throw new RuntimeException("User already has a cart. Use getCartByUser instead.");
        }

        Cart saved = cartRepo.save(toEntity(dto));
        return toDto(saved);
    }

    // ✅ NEW: Get Cart by User ID (Most important method!)
    public CartResponseDTO getCartByUser(Long userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user ID: " + userId));
        return toDto(cart);
    }

    // ✅ FIXED: Consistent total calculation
    public CartResponseDTO toDto(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setUserName(cart.getUser().getFullName());
        dto.setTotalItems(cart.getTotalItems()); // Use entity method
        dto.setTotalPrice(cart.getTotalPrice()); // Use entity field (already calculated)

        // Convert CartItems to DTOs
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(cartItemService::toDto)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    // ✅ NEW: Clear cart (keep cart, remove items)
    public CartResponseDTO clearCart(Long cartId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        Cart saved = cartRepo.save(cart);
        return toDto(saved);
    }

    // ➤ Get Cart by ID
    public CartResponseDTO getCartById(Long id) {
        Cart cart = cartRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + id));
        return toDto(cart);
    }

    // ➤ Get all Carts
    public List<CartResponseDTO> getAllCarts() {
        return cartRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // ➤ Delete Cart
    public String deleteCart(Long id) {
        Cart cart = cartRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + id));
        cartRepo.delete(cart);
        return "Cart deleted successfully.";
    }

    private Cart toEntity(CartRequestDTO dto) {
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.getUserId()));
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(BigDecimal.ZERO);
        return cart;
    }

    // ✅ Add item to cart
    public CartResponseDTO addItemToCart(Long cartId, CartItemRequestDTO itemRequest) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        Product product = productRepo.findById(itemRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        cart.addItem(product, itemRequest.getQuantity());
        Cart saved = cartRepo.save(cart);
        return toDto(saved);
    }

    // ✅ Update item quantity
    public CartResponseDTO updateCartItemQuantity(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        cart.updateItemQuantity(productId, quantity);
        Cart saved = cartRepo.save(cart);
        return toDto(saved);
    }
}
