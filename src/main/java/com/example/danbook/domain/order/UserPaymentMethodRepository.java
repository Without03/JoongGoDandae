package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Long> {
    List<UserPaymentMethod> findByUserOrderByDefaultPaymentMethodDescCreatedAtDesc(User user);
    Optional<UserPaymentMethod> findByIdAndUser(Long id, User user);
    boolean existsByUser(User user);
}
