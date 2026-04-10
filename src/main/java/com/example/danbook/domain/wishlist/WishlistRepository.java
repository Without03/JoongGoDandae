package com.example.danbook.domain.wishlist;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);
    long countByProduct(Product product);
    boolean existsByUserAndProduct(User user, Product product);
}
