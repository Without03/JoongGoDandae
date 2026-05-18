package com.example.danbook.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 로그인 처리 전에 차단 상태인지 선확인한다.
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final AntPathRequestMatcher LOGIN_POST =
            new AntPathRequestMatcher("/auth/login", "POST");

    private final LoginAttemptService loginAttemptService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (LOGIN_POST.matches(request)) {
            String username = request.getParameter("username");
            // clientIp는 현재 LoginAttemptService에서 사용하지 않는다.
            // String clientIp = clientIp(request);
            if (loginAttemptService.isBlocked(username, null)) {
                response.sendRedirect("/auth/login?blocked");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

//    private String clientIp(HttpServletRequest request) {
//        String forwardedFor = request.getHeader("X-Forwarded-For");
//        if (forwardedFor != null && !forwardedFor.isBlank()) {
//            int comma = forwardedFor.indexOf(',');
//            return (comma > 0 ? forwardedFor.substring(0, comma) : forwardedFor).trim();
//        }
//        return request.getRemoteAddr();
//    }
}

