package com.example.danbook.domain.wishlist;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 찜 엔티티.
 * 한 사용자가 같은 상품을 중복 찜할 수 없도록 (user_id, product_id)에 유니크 제약이 걸려있다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wishlists",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 찜한 사용자 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 찜한 상품 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 찜 등록 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 찜 생성 빌더. createdAt은 현재 시각으로 자동 설정된다. */
    @Builder
    public Wishlist(User user, Product product) {
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
