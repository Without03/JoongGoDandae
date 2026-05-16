package com.example.danbook.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 상품 이미지 JPA 레포지토리.
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /** 특정 상품의 모든 이미지 조회 (상품 상세 슬라이더) */
    List<ProductImage> findByProduct(Product product);

    /** 특정 상품의 첫 번째 이미지 조회 (목록 썸네일용) */
    Optional<ProductImage> findFirstByProductOrderByIdAsc(Product product);

    /** 특정 상품의 모든 이미지 삭제 (상품 삭제 또는 이미지 교체 시) */
    @Transactional
    void deleteByProduct(Product product);
}
