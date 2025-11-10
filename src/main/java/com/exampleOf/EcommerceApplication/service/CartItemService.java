package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.dto.requestdto.CartItemRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.CartItemResponseDTO;
import com.exampleOf.EcommerceApplication.entity.Cart;
import com.exampleOf.EcommerceApplication.entity.CartItem;
import com.exampleOf.EcommerceApplication.entity.FileData;
import com.exampleOf.EcommerceApplication.entity.Product;
import com.exampleOf.EcommerceApplication.repository.CartItemRepo;
import com.exampleOf.EcommerceApplication.repository.CartRepo;
import com.exampleOf.EcommerceApplication.repository.ProductRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {


    private final CartItemRepo cartItemRepo;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;


    public boolean existsById(Long id) {
        return cartItemRepo.existsById(id);
    }


    public CartItemResponseDTO addItemToCart(CartItemRequestDTO requestDTO) {
        System.out.println("🛒 === BACKEND DEBUG START ===");
        System.out.println("📥 Received request: " + requestDTO);

        Cart cart = cartRepo.findById(requestDTO.getCartId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + requestDTO.getCartId()));

        Product product = productRepo.findById(requestDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + requestDTO.getProductId()));

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepo.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        CartItem savedItem;

        if (existingItem != null) {
            // Update existing item
            existingItem.setQuantity(existingItem.getQuantity() + requestDTO.getQuantity());
            savedItem = cartItemRepo.save(existingItem);
            System.out.println("🔄 Updated existing item - ID: " + savedItem.getId());
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(requestDTO.getQuantity());
            newItem.setPricePerItem(product.getPrice());

            savedItem = cartItemRepo.save(newItem);
            System.out.println("✅ Created new item - ID: " + savedItem.getId());
        }

        // Convert to DTO
        CartItemResponseDTO responseDTO = toDto(savedItem);
        System.out.println("📤 Sending response: " + responseDTO);
        System.out.println("📤 Response ID: " + responseDTO.getId());
        System.out.println("🛒 === BACKEND DEBUG END ===\n");

        return responseDTO;
    }

    // ✅ READ: Get cart item by ID
    public CartItemResponseDTO getCartItemById(Long id) {
        CartItem cartItem = cartItemRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + id));
        return toDto(cartItem);
    }

    // ✅ READ: Get all cart items for a specific cart
    public List<CartItemResponseDTO> getCartItemsByCartId(Long cartId) {
        List<CartItem> cartItems = cartItemRepo.findByCartId(cartId);
        return cartItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ READ: Get all cart items
    public List<CartItemResponseDTO> getAllCartItems() {
        List<CartItem> cartItems = cartItemRepo.findAll();
        return cartItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ UPDATE: Update cart item quantity
    public CartItemResponseDTO updateCartItemQuantity(Long id, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + id));

        cartItem.setQuantity(newQuantity);
        CartItem updated = cartItemRepo.save(cartItem);
        return toDto(updated);
    }

    // ✅ UPDATE: Update cart item (full update)
    public CartItemResponseDTO updateCartItem(Long id, CartItemRequestDTO requestDTO) {
        CartItem cartItem = cartItemRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + id));

        if (requestDTO.getProductId() != null) {
            Product product = productRepo.findById(requestDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + requestDTO.getProductId()));
            cartItem.setProduct(product);
            cartItem.setPricePerItem(product.getPrice()); // Update price when product changes
        }

        if (requestDTO.getQuantity() != null) {
            if (requestDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            cartItem.setQuantity(requestDTO.getQuantity());
        }

        CartItem updated = cartItemRepo.save(cartItem);
        return toDto(updated);
    }

    public String deleteCartItem(Long id) {
        if (!cartItemRepo.existsById(id)) {
            return "⚠️ Cart item with ID " + id + " not found!";
        }

        cartItemRepo.deleteById(id);
        return "✅ Cart item deleted successfully!";
    }


    // ✅ DELETE: Remove cart item by cart and product
    public String deleteCartItemByCartAndProduct(Long cartId, Long productId) {
        CartItem cartItem = cartItemRepo.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found for cart ID: " + cartId + " and product ID: " + productId));

        cartItemRepo.delete(cartItem);
        return "Cart item deleted successfully!";
    }

    // ✅ DELETE: Remove all items from cart
    public String clearCartItems(Long cartId) {
        List<CartItem> cartItems = cartItemRepo.findByCartId(cartId);
        if (!cartItems.isEmpty()) {
            cartItemRepo.deleteAll(cartItems);
            return "All cart items cleared successfully!";
        }
        return "Cart is already empty!";
    }

    // ✅ COUNT: Get total items count in cart
    public Integer getCartItemsCount(Long cartId) {
        List<CartItem> cartItems = cartItemRepo.findByCartId(cartId);
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // ✅ CALCULATE: Get subtotal for cart
    public Double getCartSubtotal(Long cartId) {
        List<CartItem> cartItems = cartItemRepo.findByCartId(cartId);
        return cartItems.stream()
                .mapToDouble(item -> item.getTotalPrice().doubleValue())
                .sum();
    }

    public CartItemResponseDTO toDto(CartItem cartItem) {
        System.out.println("🔧 Converting CartItem to DTO:");
        System.out.println("   CartItem ID: " + cartItem.getId());
        System.out.println("   CartItem exists: " + (cartItem != null));

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setPricePerItem(cartItem.getPricePerItem());
        dto.setQuantity(cartItem.getQuantity());
        dto.setTotalPrice(cartItem.getTotalPrice());
        dto.setCartId(cartItem.getCart().getId());
        dto.setProductImage(getProductImageUrl(cartItem.getProduct()));

        System.out.println("   DTO ID: " + dto.getId());
        System.out.println("   🔧 Conversion complete");

        return dto;
    }

    // ✅ REUSABLE HELPER METHOD (Same as in OrderService)
    private String getProductImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return "/images/default-product.png"; // Default image
        }

        // Priority: Primary image > Sorted image > First image
        return product.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .findFirst()
                .map(FileData::getFilePath)
                .orElseGet(() ->
                        product.getImages().stream()
                                .min(Comparator.comparing(FileData::getSortOrder))
                                .map(FileData::getFilePath)
                                .orElse(product.getImages().get(0).getFilePath())
                );
    }

    // ✅ CONVERT: DTO to Entity (for internal use)
    public CartItem toEntity(CartItemRequestDTO requestDTO) {
        Cart cart = cartRepo.findById(requestDTO.getCartId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        Product product = productRepo.findById(requestDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(requestDTO.getQuantity());
        cartItem.setPricePerItem(product.getPrice());

        return cartItem;
    }

    // ✅ CHECK: If product exists in cart
    public boolean isProductInCart(Long cartId, Long productId) {
        return cartItemRepo.findByCartIdAndProductId(cartId, productId).isPresent();
    }

    // ✅ GET: CartItem by cart and product
    public CartItemResponseDTO getCartItemByCartAndProduct(Long cartId, Long productId) {
        CartItem cartItem = cartItemRepo.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found for cart ID: " + cartId + " and product ID: " + productId));
        return toDto(cartItem);
    }
}
