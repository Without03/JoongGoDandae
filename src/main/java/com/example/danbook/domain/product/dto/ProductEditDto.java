package com.example.danbook.domain.product.dto;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 상품 수정 요청 DTO.
 * ProductCreateDto에 판매 상태(status) 필드가 추가된 형태이다.
 * 이미지 파일은 컨트롤러에서 @RequestParam으로 별도 수신한다.
 */
@Getter
@Setter
public class ProductEditDto {

    /** 상품 제목 (필수, 최대 100자) */
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
    private String title;

    /** 상품 설명 (필수) */
    private String description;

    /** 판매 가격 (0원 이상) */
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;

    /** 카테고리 (필수 선택) */
    @NotNull(message = "카테고리를 선택해주세요")
    private Category category;

    /** 판매 상태 - 수정 시에만 변경 가능 (AVAILABLE / RESERVED / SOLD) */
    @NotNull(message = "상태를 선택해주세요")
    private ProductStatus status;
}
