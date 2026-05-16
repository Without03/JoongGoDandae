package com.example.danbook.domain.cart;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 장바구니 엔티티.
 * (user_id, product_id) 유니크 제약으로 중복 담기 방지.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @Builder
    public Cart(User user, Product product) {
        this.user = user;
        this.product = product;
        this.addedAt = LocalDateTime.now();
    }
}
