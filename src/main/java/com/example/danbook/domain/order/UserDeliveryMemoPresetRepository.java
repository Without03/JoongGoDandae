package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeliveryMemoPresetRepository extends JpaRepository<UserDeliveryMemoPreset, Long> {
    List<UserDeliveryMemoPreset> findByUserOrderByDefaultMemoDescCreatedAtDesc(User user);
    Optional<UserDeliveryMemoPreset> findByIdAndUser(Long id, User user);
}
