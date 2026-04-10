package com.example.danbook.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 4, max = 20, message = "아이디는 4~20자로 입력해주세요")
    private String username;
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;
    @NotBlank(message = "카카오톡 아이디를 입력해주세요")
    private String kakaoId;
}