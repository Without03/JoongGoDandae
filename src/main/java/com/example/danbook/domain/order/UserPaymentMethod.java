package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_payment_methods")
public class UserPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false)
    private boolean defaultPaymentMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserPaymentMethod(User user, PaymentMethodType type, String label, boolean defaultPaymentMethod) {
        this.user = user;
        this.type = type;
        this.label = label;
        this.defaultPaymentMethod = defaultPaymentMethod;
        this.createdAt = LocalDateTime.now();
    }

    public void clearDefault() {
        this.defaultPaymentMethod = false;
    }
}
