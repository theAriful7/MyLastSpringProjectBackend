package com.exampleOf.EcommerceApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Review extends Base{

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private Integer rating; // 1-5 rating

    // Review -> User (Many Reviews per User)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Review -> Product (Many Reviews per Product)
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
