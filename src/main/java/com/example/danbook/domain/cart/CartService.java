package com.example.danbook.domain.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.product.ProductStatus;
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

    public boolean toggle(Long productId, String username, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        validateCartable(product, quantity);

        Optional<Cart> existing = cartRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            cartRepository.delete(existing.get());
            return false;
        }

        cartRepository.save(Cart.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build());
        return true;
    }

    @Transactional(readOnly = true)
    public List<Cart> getUserCart(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }
        return cartRepository.findByUserOrderByAddedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public boolean isInCart(Long productId, String username) {
        if (username == null) {
            return false;
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        return cartRepository.existsByUserAndProduct(user, product);
    }

    public void updateQuantity(Long productId, String username, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        validateCartable(product, quantity);

        Cart cart = cartRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 담긴 상품이 아닙니다."));
        cart.updateQuantity(quantity);
    }

    public void remove(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        cartRepository.findByUserAndProduct(user, product)
                .ifPresent(cartRepository::delete);
    }

    public void clearCart(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }
        cartRepository.deleteAll(cartRepository.findByUserOrderByAddedAtDesc(user));
    }

    private void validateCartable(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("품절된 상품은 장바구니에 담을 수 없습니다.");
        }
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("남은 재고보다 많은 수량을 담을 수 없습니다.");
        }
    }
}
