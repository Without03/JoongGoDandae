package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_delivery_memo_presets")
public class UserDeliveryMemoPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String memo;

    @Column(nullable = false)
    private boolean defaultMemo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserDeliveryMemoPreset(User user, String memo, boolean defaultMemo) {
        this.user = user;
        this.memo = memo;
        this.defaultMemo = defaultMemo;
        this.createdAt = LocalDateTime.now();
    }

    public void clearDefault() {
        this.defaultMemo = false;
    }

    public void update(String memo, boolean defaultMemo) {
        this.memo = memo;
        this.defaultMemo = defaultMemo;
    }
}
