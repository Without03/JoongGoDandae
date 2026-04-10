package com.example.danbook.domain.product;

import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 중고 상품 엔티티.
 * 판매자(seller)와 ManyToOne 관계이며, 이미지는 ProductImage 엔티티에 별도 저장된다.
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

    /** 상품 카테고리 (BOOK / GOODS / OTHER) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /** 판매 상태 (AVAILABLE / RESERVED / SOLD) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    /** 판매자 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /** 등록 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 상품 생성 빌더.
     * status는 항상 AVAILABLE, createdAt은 현재 시각으로 자동 설정된다.
     */
    @Builder
    public Product(String title, String description, int price, Category category, User seller) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.seller = seller;
        this.status = ProductStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 상품 정보 수정.
     * 수정 폼 제출 시 호출되며, 판매 상태도 변경 가능하다.
     */
    public void update(String title, String description, int price, Category category, ProductStatus status) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
    }
}
