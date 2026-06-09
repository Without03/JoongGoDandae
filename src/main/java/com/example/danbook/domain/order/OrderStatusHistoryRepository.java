package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @EntityGraph(attributePaths = {"order", "order.user", "adminUser"})
    List<OrderStatusHistory> findByAdminUserOrderByChangedAtDesc(User adminUser);

    @EntityGraph(attributePaths = {"order", "adminUser"})
    List<OrderStatusHistory> findByOrderOrderByChangedAtDesc(PurchaseOrder order);
}
