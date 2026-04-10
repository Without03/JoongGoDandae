package com.example.danbook.domain.notification;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 구매 의사 알림 생성 (구매자 → 판매자)
    public void createPurchaseNotification(Long productId, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        User seller = product.getSeller();

        String message = buyer.getUsername() + "님이 '" + product.getTitle()
                + "'에 구매 의사를 보냈습니다.\n카카오톡 아이디: " + buyer.getKakaoId();

        notificationRepository.save(Notification.builder()
                .recipient(seller)
                .sender(buyer)
                .product(product)
                .message(message)
                .build());
    }

    // 알림 목록 조회 + 모두 읽음 처리
    @Transactional
    public List<Notification> getAndMarkAsRead(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(Notification::markAsRead);
        return notifications;
    }

    // 읽지 않은 알림 수 (네비게이션 뱃지용)
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }
}
