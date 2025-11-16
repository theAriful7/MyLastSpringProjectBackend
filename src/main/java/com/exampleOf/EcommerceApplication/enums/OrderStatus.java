package com.exampleOf.EcommerceApplication.enums;

public enum OrderStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}

//PENDING = 'PENDING',           // Order placed, payment pending
//CONFIRMED = 'CONFIRMED',       // Payment confirmed
//PROCESSING = 'PROCESSING',     // Vendor preparing order
//READY_FOR_SHIPMENT = 'READY_FOR_SHIPMENT', // Vendor packed order
//PICKED_UP = 'PICKED_UP',       // Rider picked up from vendor
//IN_TRANSIT = 'IN_TRANSIT',     // Rider transporting
//OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY', // Near customer location
//DELIVERED = 'DELIVERED',       // Successfully delivered
//CANCELLED = 'CANCELLED',       // Order cancelled
//RETURNED = 'RETURNED'
