package com.example.danbook.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 JPA 레포지토리.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 아이디로 회원 조회 */
    Optional<User> findByUsername(String username);

    /** 아이디 중복 여부 확인 (회원가입 시 사용) */
    boolean existsByUsername(String username);
}
