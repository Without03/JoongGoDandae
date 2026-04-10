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

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void toggle(Long productId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
        } else {
            wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
        }
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlist(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(Long productId, String username) {
        if (username == null) return false;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    @Transactional(readOnly = true)
    public long getWishlistCount(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return 0;
        return wishlistRepository.countByProduct(product);
    }
}
