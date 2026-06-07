package com.example.danbook.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 중고 상품 엔티티.
 * 이미지는 ProductImage 엔티티에 별도 저장된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품 제목 (최대 100자) */
    @Column(nullable = false)
    private String title;

    /** 상품 상세 설명 (TEXT 타입, 길이 제한 없음) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 판매 가격 (원 단위, 0 이상) */
    @Column(nullable = false)
    private int price;

    /** 할인 가격 (할인중 상태일 때만 사용, null이면 할인 없음) */
    @Column
    private Integer discountPrice;

    /** 남은 재고 수량 */
    @Column(nullable = false, columnDefinition = "int default 10")
    private int stockQuantity;

    /** 상품 대분류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MainCategory mainCategory;

    /** 상품 하위분류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /** 상품 상태 (AVAILABLE / DISCOUNTED / OUT_OF_STOCK) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    /** 등록 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 상품 생성 빌더.
     * status는 항상 AVAILABLE, createdAt은 현재 시각으로 자동 설정된다.
     */
    @Builder
    public Product(String title, String description, int price, int stockQuantity, MainCategory mainCategory, Category category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQuantity = Math.max(stockQuantity, 0);
        this.mainCategory = mainCategory;
        this.category = category;
        this.status = this.stockQuantity == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 상품 정보 수정.
     * 수정 폼 제출 시 호출되며, 상품 상태도 변경 가능하다.
     */
    public void update(String title, String description, int price, Integer discountPrice, int stockQuantity,
            MainCategory mainCategory, Category category, ProductStatus status) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQuantity = Math.max(stockQuantity, 0);
        this.mainCategory = mainCategory;
        this.category = category;
        this.status = this.stockQuantity == 0 ? ProductStatus.OUT_OF_STOCK : status;
        this.discountPrice = (status == ProductStatus.DISCOUNTED) ? discountPrice : null;
    }

    public int getEffectivePrice() {
        if (status == ProductStatus.DISCOUNTED && discountPrice != null) {
            return discountPrice;
        }
        return price;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0 || status == ProductStatus.OUT_OF_STOCK;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("구매 수량은 1개 이상이어야 합니다.");
        }
        if (stockQuantity < quantity) {
            throw new IllegalArgumentException("'" + title + "' 상품의 재고가 부족합니다.");
        }
        stockQuantity -= quantity;
        if (stockQuantity == 0) {
            status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void restock(int quantity) {
        this.stockQuantity = Math.max(quantity, 0);
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.AVAILABLE;
        }
    }
}
