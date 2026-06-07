package com.example.danbook.domain.order.dto;

import com.example.danbook.domain.order.PaymentMethodType;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private Long productId;

    @Min(value = 1, message = "구매 수량은 1개 이상이어야 합니다")
    private Integer quantity;

    private Long addressId;
    private String addressLabel;
    private String recipientName;
    private String phone;
    private String zipcode;
    private String address1;
    private String address2;
    private boolean saveAddress;
    private boolean defaultAddress;

    private Long paymentMethodId;
    private PaymentMethodType paymentMethodType;
    private String paymentMethodLabel;
    private boolean savePaymentMethod;
    private boolean defaultPaymentMethod;

    private Long memoPresetId;
    private String deliveryMemo;
    private boolean saveDeliveryMemo;
    private boolean defaultDeliveryMemo;
}
