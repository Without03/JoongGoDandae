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

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void createPurchaseNotification(Long productId, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        User admin = userRepository.findByRole(Role.ADMIN).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정이 없습니다."));

        String message = buyer.getUsername() + "님이 '" + product.getTitle()
                + "'에 구매 의사를 보냈습니다.\n이메일 주소: " + buyer.getEmail();

        notificationRepository.save(Notification.builder()
                .recipient(admin)
                .sender(buyer)
                .product(product)
                .message(message)
                .build());
    }

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

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }
}
