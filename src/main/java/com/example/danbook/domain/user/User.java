package com.example.danbook.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String kakaoId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(String username, String password, String kakaoId) {
        this.username = username;
        this.password = password;
        this.kakaoId = kakaoId;
        this.role = Role.USER;
    }
}
