package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Order;
import com.exampleOf.EcommerceApplication.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    // ✅ USER ORDERS
    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // ✅ STATUS-BASED
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // ✅ VENDOR ORDERS (through order items)
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.orderItems oi 
        JOIN oi.product p 
        WHERE p.vendor.id = :vendorId
        """)
    Page<Order> findByVendorId(@Param("vendorId") Long vendorId, Pageable pageable);

    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.orderItems oi 
        JOIN oi.product p 
        WHERE p.vendor.id = :vendorId AND o.status = :status
        """)
    Page<Order> findByVendorIdAndStatus(@Param("vendorId") Long vendorId, @Param("status") OrderStatus status, Pageable pageable);

    // ✅ DATE RANGE QUERIES
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByUserIdAndDateRange(@Param("userId") Long userId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // ✅ SEARCH ORDERS
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE CONCAT('%', :orderNumber, '%')")
    List<Order> searchByOrderNumber(@Param("orderNumber") String orderNumber);

    // ✅ ORDER STATISTICS
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :start AND :end")
    BigDecimal getRevenueInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ✅ RECENT ORDERS
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") Long userId, Pageable pageable);
}
