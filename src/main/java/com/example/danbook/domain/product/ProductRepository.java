package com.example.danbook.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 상품 JPA 레포지토리.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 특정 판매자의 상품 목록을 최신순으로 조회 (마이페이지) */
    /**
     * 키워드 + 카테고리 목록 검색, 최신순 정렬.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:categories IS NULL OR p.category IN :categories) " +
            "ORDER BY p.createdAt DESC")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("category") Category category,
                                 @Param("categories") List<Category> categories);
}
