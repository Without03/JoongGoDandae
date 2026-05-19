package com.example.danbook.domain.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoPresetForm {
    private Long id;
    private String memo;
    private boolean defaultMemo;
}
