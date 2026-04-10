package com.example.danbook.domain.notification;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 알림 JPA 레포지토리.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 특정 사용자(판매자)의 알림 목록을 최신순으로 조회 */
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    /** 특정 사용자의 읽지 않은 알림 수 (네비게이션 배지용) */
    long countByRecipientAndIsReadFalse(User recipient);
}
