package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<PurchaseOrder> findByUserOrderByOrderedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<PurchaseOrder> findAllByOrderByOrderedAtDesc();

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<PurchaseOrder> findByStatusOrderByOrderedAtDesc(OrderStatus status);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<PurchaseOrder> findWithItemsById(Long id);
}
