package com.example.danbook.global.config;

import com.example.danbook.domain.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    @ModelAttribute("unreadCount")
    public long unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return 0;
        return notificationService.getUnreadCount(userDetails.getUsername());
    }
}
