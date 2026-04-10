package com.example.danbook.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String fileName;       // UUID 기반 저장 파일명

    @Column(nullable = false)
    private String originalName;   // 원본 파일명

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Builder
    public ProductImage(Product product, String fileName, String originalName) {
        this.product = product;
        this.fileName = fileName;
        this.originalName = originalName;
        this.uploadedAt = LocalDateTime.now();
    }
}
