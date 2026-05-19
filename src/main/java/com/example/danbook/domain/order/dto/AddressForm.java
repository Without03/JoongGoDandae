package com.example.danbook.domain.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressForm {
    private Long id;
    private String label;
    private String recipientName;
    private String phone;
    private String zipcode;
    private String address1;
    private String address2;
    private boolean defaultAddress;
}
