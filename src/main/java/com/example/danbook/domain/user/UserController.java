package com.example.danbook.domain.user;

import com.example.danbook.domain.user.dto.SignupDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 회원가입 / 로그인 컨트롤러.
 * 로그인 처리 자체는 Spring Security가 담당하며,
 * 이 컨트롤러는 폼 렌더링과 회원가입 처리만 한다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    /** 회원가입 폼 렌더링 */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "auth/signup";
    }

    /**
     * 회원가입 처리.
     * 유효성 검증 실패 또는 아이디 중복 시 폼으로 돌아간다.
     * 성공 시 로그인 페이지로 리다이렉트.
     */
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

    /**
     * 로그인 폼 렌더링.
     * Spring Security가 ?error 파라미터를 붙여 실패를 전달한다.
     */
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 틀렸습니다.");
        }
        return "auth/login";
    }
}
