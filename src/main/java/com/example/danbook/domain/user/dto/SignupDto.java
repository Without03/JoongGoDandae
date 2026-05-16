package com.example.danbook.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 요청 DTO.
 * Bean Validation으로 입력값을 검증한다.
 */
@Getter
@Setter
public class SignupDto {

    /** 로그인 아이디 (4~20자) */
    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 4, max = 20, message = "아이디는 4~20자로 입력해주세요")
    private String username;

    /** 비밀번호 (8자 이상, 저장 시 BCrypt 암호화) */
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    /** 카카오톡 아이디 (구매 의사 알림에 사용) */
    @NotBlank(message = "카카오톡 아이디를 입력해주세요")
    private String kakaoId;
}
