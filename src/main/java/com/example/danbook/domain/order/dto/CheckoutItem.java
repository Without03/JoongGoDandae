package com.example.danbook.domain.order.dto;

import com.example.danbook.domain.product.Product;
import lombok.Getter;

@Getter
public class CheckoutItem {

    private final Product product;
    private final int quantity;
    private final int unitPrice;

    public CheckoutItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getEffectivePrice();
    }

    public int getLineTotal() {
        return unitPrice * quantity;
    }
}
