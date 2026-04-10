package com.example.danbook.domain.wishlist;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 찜 관련 비즈니스 로직 서비스.
 * 찜 토글, 찜 목록 조회, 찜 여부 확인, 찜 수 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 찜 토글.
     * 이미 찜한 상품이면 찜 해제, 아니면 찜 추가.
     */
    public void toggle(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            // 이미 찜한 경우 → 해제
            wishlistRepository.delete(existing.get());
        } else {
            // 찜하지 않은 경우 → 추가
            wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
        }
    }

    /**
     * 사용자의 찜 목록 조회 (최신순).
     * 마이페이지에서 사용한다.
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlist(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 사용자가 해당 상품을 찜했는지 여부 확인.
     * 비로그인(username=null) 시 항상 false 반환.
     */
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long productId, String username) {
        if (username == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    /**
     * 특정 상품의 총 찜 수 조회 (상품 상세 페이지 표시용).
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0;
        return wishlistRepository.countByProduct(product);
    }
}
