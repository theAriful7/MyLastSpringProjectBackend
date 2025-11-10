package com.exampleOf.EcommerceApplication.repository;


import com.exampleOf.EcommerceApplication.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
