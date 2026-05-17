package com.example.danbook.domain.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 장바구니 토글.
     * 이미 담긴 상품이면 제거, 아니면 추가. 결과로 현재 상태(true=담김) 반환.
     */
    public boolean toggle(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Optional<Cart> existing = cartRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            cartRepository.delete(existing.get());
            return false; // 제거됨
        } else {
            cartRepository.save(Cart.builder().user(user).product(product).build());
            return true;  // 추가됨
        }
    }

    /** 사용자의 장바구니 목록 조회 (최신순) */
    @Transactional(readOnly = true)
    public List<Cart> getUserCart(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return cartRepository.findByUserOrderByAddedAtDesc(user);
    }

    /** 특정 상품이 장바구니에 담겨있는지 확인 */
    @Transactional(readOnly = true)
    public boolean isInCart(Long productId, String username) {
        if (username == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        return cartRepository.existsByUserAndProduct(user, product);
    }

    /** 장바구니에서 특정 상품 제거 */
    public void remove(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        cartRepository.findByUserAndProduct(user, product)
                .ifPresent(cartRepository::delete);
    }
    /** 장바구니 전체 비우기 */
    public void clearCart(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return;
        cartRepository.deleteAll(cartRepository.findByUserOrderByAddedAtDesc(user));
    }
}
