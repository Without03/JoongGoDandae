package com.example.danbook.domain.product;

public enum ProductStatus {
    AVAILABLE("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
