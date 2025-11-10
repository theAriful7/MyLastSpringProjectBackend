package com.exampleOf.EcommerceApplication.entity;

import com.exampleOf.EcommerceApplication.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors")
@Data
public class Vendor {
    // Getters and Setters
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shopName;

    @Setter
    private String businessDescription;

    @Setter
    @Column(unique = true)
    private String taxNumber;

    @Setter
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus vendorStatus;

    @OneToMany(mappedBy = "vendor")
    private List<Product> products = new ArrayList<>();

    @Setter
    private boolean approved = false;

    // One-to-One with User
    @Setter
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Vendor() {}

    public Vendor(String shopName, User user) {
        this.shopName = shopName;
        this.user = user;
    }

    public void setBusinessName(String businessName) { this.shopName = shopName; }

    public boolean isActive() {
        return this.vendorStatus == VendorStatus.ACTIVE;
    }

}