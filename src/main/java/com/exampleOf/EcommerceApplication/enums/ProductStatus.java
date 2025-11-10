package com.exampleOf.EcommerceApplication.enums;

public enum ProductStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED,
    REJECTED, APPROVED;


    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isVisible() {
        return this == ACTIVE || this == OUT_OF_STOCK;
    }

    public boolean canBePurchased() {
        return this == ACTIVE;
    }
}
