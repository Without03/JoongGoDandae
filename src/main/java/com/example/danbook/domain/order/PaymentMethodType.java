package com.example.danbook.domain.order;

public enum PaymentMethodType {
    CARD("카드결제"),
    BANK_TRANSFER("무통장입금"),
    EASY_PAY("간편결제"),
    CASH_ON_DELIVERY("수령 후 결제");

    private final String displayName;

    PaymentMethodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
