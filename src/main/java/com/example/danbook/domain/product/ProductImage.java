package com.example.danbook.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 이미지 엔티티.
 * 하나의 상품에 여러 이미지가 등록될 수 있다 (ManyToOne).
 * 실제 파일은 /uploads/ 디렉터리에 UUID 파일명으로 저장된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 연관된 상품 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** UUID 기반 저장 파일명 (실제 파일 경로에 사용) */
    @Column(nullable = false)
    private String fileName;

    /** 사용자가 업로드한 원본 파일명 */
    @Column(nullable = false)
    private String originalName;

    /** 업로드 일시 */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * 이미지 생성 빌더.
     * uploadedAt은 현재 시각으로 자동 설정된다.
     */
    @Builder
    public ProductImage(Product product, String fileName, String originalName) {
        this.product = product;
        this.fileName = fileName;
        this.originalName = originalName;
        this.uploadedAt = LocalDateTime.now();
    }
}
