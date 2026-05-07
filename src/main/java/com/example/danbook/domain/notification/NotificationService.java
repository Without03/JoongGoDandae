package com.example.danbook.domain.notification;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 관련 비즈니스 로직 서비스.
 * 구매 의사 알림 생성, 알림 목록 조회(읽음 처리), 미읽음 수 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 구매 의사 알림 생성 (구매자 → 판매자).
     * 메시지에 구매자의 카카오톡 아이디가 포함되어 판매자에게 전달된다.
     */
    public void createPurchaseNotification(Long productId, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        User admin = userRepository.findByRole(Role.ADMIN).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정이 없습니다."));

        String message = buyer.getUsername() + "님이 '" + product.getTitle()
                + "'에 구매 의사를 보냈습니다.\n카카오톡 아이디: " + buyer.getKakaoId();

        notificationRepository.save(Notification.builder()
                .recipient(admin)
                .sender(buyer)
                .product(product)
                .message(message)
                .build());
    }

    /**
     * 알림 목록 조회 + 전체 읽음 처리.
     * 알림 탭 진입 시 호출되며, 미읽음 알림을 모두 읽음으로 변경한다.
     */
    @Transactional
    public List<Notification> getAndMarkAsRead(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        // 읽지 않은 알림만 읽음 처리
        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(Notification::markAsRead);
        return notifications;
    }

    /**
     * 읽지 않은 알림 수 조회.
     * @ControllerAdvice에서 모든 페이지의 네비게이션 배지에 사용된다.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }
}
