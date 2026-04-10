package com.example.danbook.domain.product;

/**
 * 상품 판매 상태 enum.
 * 등록 시 기본값은 AVAILABLE이며, 수정 폼에서 변경할 수 있다.
 */
public enum ProductStatus {
    AVAILABLE("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    /** 화면에 표시할 한글 상태명 */
    public String getDisplayName() {
        return displayName;
    }
}
