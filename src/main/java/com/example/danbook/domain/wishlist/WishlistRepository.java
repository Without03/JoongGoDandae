package com.example.danbook.domain.wishlist;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 찜 JPA 레포지토리.
 */
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /** 특정 사용자 + 상품의 찜 레코드 조회 (찜 토글 시 사용) */
    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    /** 특정 사용자의 찜 목록을 최신순으로 조회 (마이페이지) */
    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);

    /** 특정 상품의 찜 수 조회 (상품 상세 페이지) */
    long countByProduct(Product product);

    /** 특정 사용자가 상품을 찜했는지 여부 확인 */
    boolean existsByUserAndProduct(User user, Product product);
    List<Wishlist> findByProduct(Product product);
}
