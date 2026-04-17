package com.example.danbook.global.config;

import com.example.danbook.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정.
 * - /products/**, /auth/login, /auth/signup 등은 비로그인 접근 허용
 * - 그 외 경로는 로그인 필요 (/ 포함)
 * - 로그아웃은 POST /auth/logout, 성공 시 로그인 페이지로 이동
 */
/*
 Spring Security 설명:
 Authentication 인가, 인증
 특정 경로에 요청이 오면 filter에서 spring security가 가로채서
 사용자가 해당 경로에 접근할 수 있는지 확인한다.
 SecurityConfig 클래스 작성
 */
@Configuration // spring 설정 클래스
@EnableWebSecurity // Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean // pw에 대해 단방향 해시 암호화 진행, 저장된 pw와 대조
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // filterChain 메서드로 SecurityFilterChain 인터페이스 구현
        // 인자로 HttpSecurity를 받고 빌더 타입 리턴
        http
                .authorizeHttpRequests(auth -> auth // 요청 인가 규칙 설정 메소드, 요청이 특정 url 패턴과 일치하는지 여부에 따라 접근 권한 설정
                        // '/', '/login' 모든 유저, '/admin' ADMIN role을 가진 유저, '/my' admin, user role 가진 유저
                        .requestMatchers(
                                "/auth/login",
                                "/auth/signup",
                                "/products/**",   // 상품 목록·상세는 비로그인 접근 허용
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/h2-console/**"  // H2 콘솔도 허용
                        ).permitAll()
                        .anyRequest().authenticated()  // 나머지(/, /mypage, /notifications 등)는 로그인 필요
                )
                .formLogin(form -> form // formLogin() 메서드로 로그인 페이지, url 성공/실패시 동작 정의
                        .loginPage("/auth/login")           // 커스텀 로그인 페이지
                        .loginProcessingUrl("/auth/login")  // POST 로그인 처리 URL
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)       // 로그인 성공 시 메인 페이지로
                        .failureUrl("/auth/login?error")    // 실패 시 ?error 파라미터 추가
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")               // POST만 허용
                        .logoutSuccessUrl("/auth/login?logout")  // 로그아웃 성공 시 로그인 페이지로 (리다이렉트 루프 방지)
                        .permitAll()
                )
                // H2 콘솔 사용을 위한 설정 (CSRF 제외, X-Frame-Options 비활성화)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }


}
