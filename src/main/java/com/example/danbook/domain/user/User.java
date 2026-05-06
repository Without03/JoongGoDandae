package com.example.danbook.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티.
 * 아이디(username), 비밀번호, 카카오톡 아이디, 권한(role)을 보유한다.
 * 카카오톡 아이디는 구매 의사 전달 시 판매자에게만 알림으로 노출된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디 (유일값) */
    @Column(unique = true, nullable = false)
    private String username;

    /** BCrypt 암호화된 비밀번호 */
    @Column(nullable = false)
    private String password;

    /** 카카오톡 아이디 (구매자 → 판매자 알림에만 사용, 목록/상세 페이지에는 비공개) */
    @Column(nullable = false)
    private String kakaoId;

    /** 권한 (USER / ADMIN) */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * 회원 생성 빌더.
     * role은 항상 USER로 초기화된다.
     */
    @Builder
    public User(String username, String password, String kakaoId, Role role) {
        this.username = username;
        this.password = password;
        this.kakaoId = kakaoId;
        this.role = role == null ? Role.USER : role;
    }
}
