package com.example.danbook.domain.user;

import com.example.danbook.domain.user.dto.SignupDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    // 회원가입 폼
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "auth/signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupDto signupDto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            userService.signup(signupDto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/signup";
        }

        return "redirect:/auth/login";
    }

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 틀렸습니다.");
        }
        return "auth/login";
    }
}

