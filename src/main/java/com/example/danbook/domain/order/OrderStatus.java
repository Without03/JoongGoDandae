package com.example.danbook.domain.order;

public enum OrderStatus {
    RECEIVED("주문접수"),
    PAYMENT_PENDING("결제대기"),
    PAID("결제완료"),
    PREPARING("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료"),
    CANCELED("취소");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
