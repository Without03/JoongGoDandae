package com.example.danbook.domain.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.User;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserOrderByAddedAtDesc(User user);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);
}
