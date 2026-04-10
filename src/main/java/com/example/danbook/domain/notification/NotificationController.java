package com.example.danbook.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 알림 컨트롤러.
 * /notifications 경로를 담당하며 로그인이 필요하다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 페이지.
     * 진입 시 모든 미읽음 알림을 읽음으로 일괄 처리한다.
     */
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        model.addAttribute("notifications",
                notificationService.getAndMarkAsRead(userDetails.getUsername()));
        return "notification/list";
    }
}
