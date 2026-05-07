package com.example.danbook.domain.product;

public enum ProductStatus {
    AVAILABLE("판매중"),
    DISCOUNTED("할인중"),
    OUT_OF_STOCK("품절");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
