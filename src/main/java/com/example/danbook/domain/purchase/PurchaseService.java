package com.example.danbook.domain.purchase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구매 내역 비즈니스 로직 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 구매 내역 기록.
     * 사용자가 구매 버튼을 누를 때 호출된다.
     */
    public void recordPurchase(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        purchaseRepository.save(Purchase.builder().user(user).product(product).build());
    }

    /**
     * 사용자의 구매 내역 조회 (최신순).
     */
    @Transactional(readOnly = true)
    public List<Purchase> getUserPurchases(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return purchaseRepository.findByUserOrderByPurchasedAtDesc(user);
    }
}