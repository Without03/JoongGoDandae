package com.example.danbook.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) return;

        User seller = userRepository.findByUsername("testuser")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("testuser")
                                .password(passwordEncoder.encode("password1234"))
                                .kakaoId("testuser_kakao")
                                .build()
                ));

        Object[][] items = {
                // 뒤쪽 번호 아이템들부터 먼저 배치 (아래로 내려감)
                {"2000리갈패드 A4 노랑 60매",                2500,   Category.OTHER},
                {"단국대 멀티뉴트리션",                     120000,  Category.OTHER},
                {"단국대 엔닥터 멀티비타민 엔올인원 (90정)",  90000,  Category.OTHER},
                {"단국대 라이프앤헬스 엔올인원 30병×1박스",  540000, Category.OTHER},
                {"블루투스 키보드",                          18000,  Category.OTHER},
                {"단국대 타올 (DKU 로고)",                   5900,   Category.GOODS},
                {"손톱깎이 세트",                            16500,  Category.GOODS},
                {"수저세트",                                  8500,   Category.GOODS},
                {"이지롤 티스푼포크 (4P)",                   16000,  Category.GOODS},
                {"단국대 에코백",                             8000,   Category.GOODS},
                {"단국대 골프공 타이틀리스트 (6구)",          42000,  Category.GOODS},
                {"DKU 스포츠 마사지크림",                    23000,   Category.GOODS},
                {"단국대 스위스밀리터리 3단 우산",            16000,  Category.GOODS},
                {"핑크솔트 블랙페퍼 그라인더 세트",           22000,  Category.GOODS},
                {"단국대 PGA 지갑 우산",                     20000,   Category.GOODS},
                {"단국대 박물관 메모지",                      15000,  Category.GOODS},
                {"단국대학교 세면파우치",                      5000,   Category.GOODS},
                {"단국대 리유저블 텀블러",                     4900,   Category.GOODS},
                {"단국대 명찰 목걸이",                         3000,   Category.GOODS},
                {"단국대 벽시계",                            35000,   Category.GOODS},
                {"단국대 여행용 어댑터",                      35000,  Category.GOODS},
                {"단국대 미니 에코백",                         5000,   Category.GOODS},
                {"단국대 넥타이",                            25000,   Category.GOODS},
                {"단국대 캔버스 토트백",                      88000,  Category.GOODS},
                {"단국대 엽서",                               1000,   Category.GOODS},
                {"단국대 포스트잇",                           2500,   Category.GOODS},
                {"단국대 가죽 필통",                           9900,   Category.GOODS},
                {"단국대 앞치마",                            10000,   Category.GOODS},
                {"단국대 뱃지",                               3000,   Category.GOODS},
                {"보조배터리 (리뉴얼)",                       14000,  Category.GOODS},
                {"아크릴 마그넷",                             3000,   Category.GOODS},
                {"단테디 키링",                               9000,   Category.GOODS},
                {"단국대 문진",                              45000,   Category.GOODS},
                {"따뜻하곰 핫팩",                             1200,   Category.GOODS},
                {"단국대 2026년 달력",                        6000,   Category.GOODS},
                {"단국대 부채",                               1000,   Category.GOODS},
                {"단국대 메쉬파우치",                         7900,   Category.GOODS},
                {"단국대 릴홀더 명찰목걸이",                   5000,   Category.GOODS},
                {"단국대 타올",                               5900,   Category.GOODS},
                {"단국대 아크릴키링",                         5500,   Category.GOODS},
                {"단국대 에폭시 마그넷",                       4000,   Category.GOODS},
                {"단국대 양말",                               4500,   Category.GOODS},
                {"단국대 스누피 세면타올",                     6900,   Category.GOODS},
                {"단국대 아크릴 네임텍",                      15000,  Category.GOODS},
                {"단국대 벽시계 (캠퍼스맵)",                  38000,  Category.GOODS},
                {"단국대 머그컵 (캠퍼스맵)",                   8000,   Category.GOODS},
                {"단국대 인형키링 (MZ/AZ)",                   8000,   Category.GOODS},
                {"단국대 레트로 선풍기",                      34800,  Category.GOODS},
                {"단잔 (소주잔)",                             4000,   Category.GOODS},
                {"단국대 커스텀 젤펜",                        2200,   Category.GOODS},
        };

        for (Object[] item : items) {
            productRepository.save(
                    Product.builder()
                            .title((String) item[0])
                            .description((String) item[0] + " 판매합니다. 직거래 또는 택배 거래 가능합니다.")
                            .price((Integer) item[1])
                            .category((Category) item[2])
                            .seller(seller)
                            .build()
            );
        }
    }
}