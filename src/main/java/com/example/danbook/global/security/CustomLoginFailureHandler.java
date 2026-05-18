package com.example.danbook.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 시 횟수를 누적하고, 차단 상태면 blocked 파라미터로 안내한다.
 */
@Component
@RequiredArgsConstructor
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        // clientIp는 현재 LoginAttemptService에서 사용하지 않는다.
        // String clientIp = clientIp(request);

        loginAttemptService.onFailure(username, null);

        if (loginAttemptService.isBlocked(username, null)) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?blocked");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/auth/login?error");
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

