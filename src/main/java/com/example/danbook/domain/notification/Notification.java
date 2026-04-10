package com.example.danbook.domain.notification;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 엔티티.
 * 구매자가 구매 의사를 보내면 판매자(recipient)에게 알림이 생성된다.
 * message에는 구매자의 카카오톡 아이디가 포함된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 수신자 - 판매자 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** 알림 발신자 - 구매자 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** 구매 의사를 보낸 상품 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 알림 내용 (구매자 아이디 + 카카오톡 아이디 포함) */
    @Column(nullable = false)
    private String message;

    /** 읽음 여부 (알림 탭 접근 시 true로 변경) */
    @Column(nullable = false)
    private boolean isRead;

    /** 알림 생성 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 알림 생성 빌더.
     * isRead는 false, createdAt은 현재 시각으로 자동 설정된다.
     */
    @Builder
    public Notification(User recipient, User sender, Product product, String message) {
        this.recipient = recipient;
        this.sender = sender;
        this.product = product;
        this.message = message;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    /** 알림을 읽음 상태로 변경한다. */
    public void markAsRead() {
        this.isRead = true;
    }
}
