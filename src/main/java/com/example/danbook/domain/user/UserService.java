package com.example.danbook.domain.user;

import com.example.danbook.domain.user.dto.SignupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

