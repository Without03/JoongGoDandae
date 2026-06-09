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
@Table(name = "order_status_histories")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    @Column(length = 300)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Builder
    public OrderStatusHistory(PurchaseOrder order, User adminUser, OrderStatus fromStatus,
                              OrderStatus toStatus, String memo) {
        this.order = order;
        this.adminUser = adminUser;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.memo = memo;
        this.changedAt = LocalDateTime.now();
    }
}
