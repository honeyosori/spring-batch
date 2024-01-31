package com.spring.batch.order.module.constants;

public enum OrderStatus {

    ORDERED("Ordered"),
    PENDING("Pending"),
    SHIPPING("Shipping"),
    DELIVERED("Delivered"),
    CANCELED("Canceled");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
