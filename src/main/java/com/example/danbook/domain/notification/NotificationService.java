package com.example.danbook.domain.notification;

import com.example.danbook.domain.cart.CartRepository;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.product.ProductStatus;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import com.example.danbook.domain.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;

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

    public void createProductStatusChangeNotifications(Long productId,
                                                       ProductStatus oldStatus,
                                                       ProductStatus newStatus,
                                                       String senderUsername) {
        if (oldStatus == newStatus) {
            return;
        }

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Map<Long, User> recipientsById = wishlistRepository.findByProduct(product).stream()
                .map(wishlist -> wishlist.getUser())
                .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));

        cartRepository.findByProduct(product).stream()
                .map(cart -> cart.getUser())
                .forEach(user -> recipientsById.putIfAbsent(user.getId(), user));

        List<Notification> notifications = recipientsById.values().stream()
                .filter(user -> user.getRole() == Role.USER)
                .map(user -> Notification.builder()
                        .recipient(user)
                        .sender(sender)
                        .product(product)
                        .message("'" + product.getTitle() + "' 상품의 상태가 "
                                + oldStatus.getDisplayName() + "에서 "
                                + newStatus.getDisplayName() + "(으)로 변경되었습니다.")
                        .build())
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
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
