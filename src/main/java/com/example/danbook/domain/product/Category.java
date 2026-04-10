package com.example.danbook.domain.product;

public enum Category {
    BOOK("교재/도서"),
    GOODS("굿즈"),
    OTHER("기타");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
