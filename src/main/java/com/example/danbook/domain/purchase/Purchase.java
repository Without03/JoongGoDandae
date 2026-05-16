package com.example.danbook.domain.purchase;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구매 내역 엔티티.
 * 사용자가 구매 버튼을 누를 때마다 기록된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구매한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 구매한 상품 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 구매 일시 */
    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @Builder
    public Purchase(User user, Product product) {
        this.user = user;
        this.product = product;
        this.purchasedAt = LocalDateTime.now();
    }
}

