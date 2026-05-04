package com.example.danbook.domain.product;

/**
 * 상품 카테고리 enum.
 * 검색/필터에 사용되며 displayName은 화면에 표시되는 한글명이다.
 */
/* Category
    Food, Household, Fashion, Cosmetics, Digital, Sports, Furniture
 */
public enum Category {
    BOOK("교재/도서"),
    GOODS("굿즈"),
    OTHER("기타")
    ;




    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /** 화면에 표시할 한글 카테고리명 */
    public String getDisplayName() {
        return displayName;
    }
}
