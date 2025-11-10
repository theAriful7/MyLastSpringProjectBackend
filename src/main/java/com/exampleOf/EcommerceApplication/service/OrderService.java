package com.exampleOf.EcommerceApplication.service;

import com.exampleOf.EcommerceApplication.dto.requestdto.OrderRequestDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.OrderItemResponseDTO;
import com.exampleOf.EcommerceApplication.dto.responsedto.OrderResponseDTO;
import com.exampleOf.EcommerceApplication.entity.*;
import com.exampleOf.EcommerceApplication.enums.OrderStatus;
import com.exampleOf.EcommerceApplication.enums.ProductStatus;
import com.exampleOf.EcommerceApplication.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final UserRepository userRepo;
    private final ProductRepo productRepo;
    private final OrderItemRepo orderItemRepo;
    private final AddressRepo addressRepo;
    private final CartService cartService;
    private final CartItemRepo cartItemRepo;

    // ✅ CREATE ORDER FROM REQUEST DTO (Controller calls this as save())
    @Transactional
    public OrderResponseDTO save(OrderRequestDTO dto) {
        return createOrder(dto);
    }

    // ✅ CREATE ORDER FROM CART (MAIN CHECKOUT METHOD)
    @Transactional
    public OrderResponseDTO checkout(Long userId, Long addressId) {
        return checkoutFromCart(userId, addressId);
    }

    // ✅ CREATE ORDER FROM CART (Implementation)
    @Transactional
    public OrderResponseDTO checkoutFromCart(Long userId, Long addressId) {
        // 1. Get user and validate
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. Get shipping address
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Shipping address not found with id: " + addressId));

        // 3. Get user's cart with items
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Add products before checkout.");
        }

        // 4. Validate stock and prices
        validateCartItems(cart);

        // 5. Create order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);

        // 6. Convert CartItems to OrderItems (LOCK PRICES)
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = convertToOrderItem(cartItem, order);
            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            // 7. Update product stock
            updateProductStock(cartItem.getProduct(), cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);

        // 8. Save order (cascades to order items)
        Order savedOrder = orderRepo.save(order);

        // 9. Clear cart after successful order
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartItemRepo.deleteAllByCartId(cart.getId());

        return toDto(savedOrder);
    }

    // ✅ CONVERT CART ITEM TO ORDER ITEM (CRITICAL - LOCKS PRICE)
    private OrderItem convertToOrderItem(CartItem cartItem, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());

        // ✅ CRITICAL: Use price from CartItem, NOT current product price
        orderItem.setPrice(cartItem.getPricePerItem());
        orderItem.setTotalPrice(cartItem.getTotalPrice());

        return orderItem;
    }

    // ✅ VALIDATE CART ITEMS BEFORE ORDER
    private void validateCartItems(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Check stock availability
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStock() + ", Requested: " + cartItem.getQuantity()
                );
            }

            // Check if product is active/available (FIXED - use direct comparison)
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new RuntimeException("Product not available: " + product.getName());
            }
        }
    }

    // ✅ UPDATE PRODUCT STOCK
    private void updateProductStock(Product product, Integer quantitySold) {
        product.setStock(product.getStock() - quantitySold);
        productRepo.save(product);
    }

    // ✅ CREATE ORDER MANUALLY (Admin/Backend use)
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Address address = addressRepo.findById(dto.getShippingAddressId())
                .orElseThrow(() -> new RuntimeException("Shipping address not found"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemDTO : dto.getItems()) {
            Product product = productRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Validate stock for manual orders too
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice()); // Use current price for manual orders
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            // Update stock
            updateProductStock(product, itemDTO.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepo.save(order);

        return toDto(savedOrder);
    }

    // ✅ GET ORDER BY ID (Controller calls this as findById())
    public OrderResponseDTO findById(Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return toDto(order);
    }

    // ✅ GET ALL ORDERS (Controller calls this as findAll())
    public List<OrderResponseDTO> findAll() {
        return orderRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ GET ORDERS BY USER
    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepo.findAll().stream()
                .filter(order -> order.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ✅ UPDATE ORDER STATUS (Controller calls this as updateStatus())
    @Transactional
    public OrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Add status transition validation if needed
        order.setStatus(newStatus);
        Order updated = orderRepo.save(order);

        return toDto(updated);
    }

    // ✅ CANCEL ORDER
    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Only allow canceling pending orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepo.save(order);

        return toDto(updated);
    }

    // ✅ DELETE ORDER (Controller calls this as delete())
    @Transactional
    public void delete(Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        orderRepo.delete(order);
    }

    // ✅ CONVERT TO DTO
    public OrderResponseDTO toDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());

        // Convert order items
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    // ✅ CONVERT ORDER ITEM TO DTO
    private OrderItemResponseDTO toOrderItemDto(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());

        // Add product image using helper method
        dto.setProductImage(getProductImageUrl(item.getProduct()));

        return dto;
    }

    // ✅ HELPER METHOD TO GET PRODUCT IMAGE URL
    private String getProductImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        // Try to get primary image first
        Optional<FileData> primaryImage = product.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .findFirst();

        if (primaryImage.isPresent()) {
            return primaryImage.get().getFilePath();
        }

        // If no primary, get first image by sort order
        return product.getImages().stream()
                .min(Comparator.comparing(FileData::getSortOrder))
                .map(FileData::getFilePath)
                .orElseGet(() -> {
                    // Fallback: get any image
                    return product.getImages().get(0).getFilePath();
                });
    }
}
