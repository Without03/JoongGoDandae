package com.example.danbook.domain.purchase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.danbook.domain.user.User;

/**
 * 구매 내역 JPA 레포지토리.
 */
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** 특정 사용자의 구매 내역을 최신순으로 조회 (마이페이지) */
    List<Purchase> findByUserOrderByPurchasedAtDesc(User user);
}

