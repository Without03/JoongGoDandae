package com.example.danbook.domain.product;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 상품 JPA 레포지토리.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 특정 판매자의 상품 목록을 최신순으로 조회 (마이페이지) */
    List<Product> findBySellerOrderByCreatedAtDesc(User seller);

    /**
     * 키워드 + 카테고리 복합 검색, 최신순 정렬.
     * keyword가 null이면 전체 조회, category가 null이면 전체 카테고리.
     * 제목과 설명 모두 대소문자 무시 LIKE 검색한다.
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "ORDER BY p.createdAt DESC")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("category") Category category);
}
