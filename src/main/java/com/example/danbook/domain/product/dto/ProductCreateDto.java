package com.example.danbook.domain.product.dto;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.MainCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateDto {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;

    @NotNull(message = "대분류를 선택해주세요")
    private MainCategory mainCategory;

    @NotNull(message = "카테고리를 선택해주세요")
    private Category category;
}
