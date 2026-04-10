package com.example.danbook.domain.product;

import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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

    public void update(String title, String description, int price, Category category, ProductStatus status) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
    }
}
