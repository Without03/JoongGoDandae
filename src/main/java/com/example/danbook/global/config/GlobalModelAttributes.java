package com.example.danbook.global.config;

import com.example.danbook.domain.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 컨트롤러에 공통으로 주입되는 모델 속성 정의.
 * @ControllerAdvice를 통해 모든 뷰에 unreadCount를 자동으로 전달한다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    /**
     * 읽지 않은 알림 수를 "unreadCount" 이름으로 모든 모델에 주입.
     * 네비게이션 바의 알림 배지에 표시된다.
     * 비로그인 시 0 반환.
     */
    @ModelAttribute("unreadCount")
    public long unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return 0;
        return notificationService.getUnreadCount(userDetails.getUsername());
    }
}
