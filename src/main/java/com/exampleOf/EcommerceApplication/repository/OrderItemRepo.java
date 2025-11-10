package com.exampleOf.EcommerceApplication.repository;

import com.exampleOf.EcommerceApplication.entity.Order;
import com.exampleOf.EcommerceApplication.entity.OrderItem;
import com.exampleOf.EcommerceApplication.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);
    List<OrderItem> findByOrder(Order order);
    void deleteByOrderAndProduct(Order order, Product product); // Optional: for direct deletion

}
