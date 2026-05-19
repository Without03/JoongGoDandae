package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserOrderByDefaultAddressDescCreatedAtDesc(User user);
    Optional<UserAddress> findByIdAndUser(Long id, User user);
    boolean existsByUser(User user);
}
