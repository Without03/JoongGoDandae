package com.example.danbook.domain.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.danbook.domain.user.dto.SignupDto;

import lombok.RequiredArgsConstructor;

/**
 * 회원 관련 비즈니스 로직 서비스.
 * 회원가입, 회원 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 아이디로 회원 엔티티 조회.
     * 존재하지 않으면 IllegalArgumentException 발생.
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    /**
     * 회원가입 처리.
     * 아이디 중복 시 IllegalArgumentException 발생.
     * 비밀번호는 BCrypt로 암호화하여 저장한다.
     */
    public void signup(SignupDto dto) {

        // 아이디 중복 체크
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .kakaoId(dto.getKakaoId())
                .build();

        userRepository.save(user);
    }
    /**
    * 사용자 정보 수정.
    * 카카오톡 아이디, 비밀번호(선택) 변경.
    * 현재 비밀번호 확인 후 불일치 시 예외 발생.
    */
    public void updateUser(String username, String newKakaoId, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 비밀번호 변경을 시도하는 경우 현재 비밀번호 검증
    String encodedNew = null;
    if (newPassword != null && !newPassword.isBlank()) {
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        encodedNew = passwordEncoder.encode(newPassword);
    }

    user.update(newKakaoId, encodedNew);
}
}

