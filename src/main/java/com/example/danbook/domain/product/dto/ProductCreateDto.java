package com.example.danbook.domain.product.dto;

import com.example.danbook.domain.product.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 상품 등록 요청 DTO.
 * 이미지 파일은 컨트롤러에서 @RequestParam으로 별도 수신한다.
 */
@Getter
@Setter
public class ProductCreateDto {

    /** 상품 제목 (필수, 최대 100자) */
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
    private String title;

    /** 상품 설명 (필수) */
    @NotBlank(message = "설명을 입력해주세요")
    private String description;

    /** 판매 가격 (0원 이상) */
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;

    /** 카테고리 (필수 선택) */
    @NotNull(message = "카테고리를 선택해주세요")
    private Category category;
}
