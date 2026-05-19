package com.example.danbook.domain.order.dto;

import com.example.danbook.domain.order.PaymentMethodType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodForm {
    private Long id;
    private PaymentMethodType type;
    private String label;
    private boolean defaultPaymentMethod;
}
