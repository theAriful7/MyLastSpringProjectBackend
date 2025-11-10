package com.exampleOf.EcommerceApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
@EqualsAndHashCode(callSuper = true)
public class Cart extends Base{


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    // ✅ FIXED: Better item management WITH PRICE
    public void addItem(Product product, Integer quantity) {
        // Get current product price
        BigDecimal currentPrice = product.getPrice();

        // Check if item already exists
        Optional<CartItem> existingItem = findItemByProductId(product.getId());

        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Create new cart item with price
            CartItem newItem = new CartItem();
            newItem.setCart(this);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPricePerItem(currentPrice); // ✅ STORE THE PRICE!

            items.add(newItem);
        }
        recalculateTotal();
    }

    // ✅ FIXED: Add item with CartItem object (if needed)
    public void addItem(CartItem item) {
        // Ensure price is set from product
        if (item.getPricePerItem() == null) {
            item.setPricePerItem(item.getProduct().getPrice());
        }

        Optional<CartItem> existingItem = findItemByProductId(item.getProduct().getId());

        if (existingItem.isPresent()) {
            CartItem existing = existingItem.get();
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            items.add(item);
            item.setCart(this);
        }
        recalculateTotal();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateTotal();
    }

    // ✅ Remove item by product ID
    public void removeItemByProductId(Long productId) {
        items.removeIf(item -> {
            boolean shouldRemove = item.getProduct().getId().equals(productId);
            if (shouldRemove) {
                item.setCart(null);
            }
            return shouldRemove;
        });
        recalculateTotal();
    }

    // ✅ Update item quantity
    public void updateItemQuantity(Long productId, Integer newQuantity) {
        if (newQuantity <= 0) {
            removeItemByProductId(productId);
            return;
        }

        findItemByProductId(productId).ifPresent(item -> {
            item.setQuantity(newQuantity);
            recalculateTotal();
        });
    }

    public void recalculateTotal() {
        this.totalPrice = items.stream()
                .map(CartItem::getTotalPrice) // This uses getTotalPrice() from CartItem
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // ✅ Find item by product
    public Optional<CartItem> findItemByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }
}
