package com.example.danbook.domain.order;

public enum OrderStatus {
    RECEIVED("주문 접수"),
    PAYMENT_PENDING("결제 대기"),
    PAID("결제 완료"),
    PREPARING("배송 준비 중"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    CANCEL_REQUESTED("취소 신청"),
    CANCELED("취소");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
