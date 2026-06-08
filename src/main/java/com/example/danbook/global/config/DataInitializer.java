package com.example.danbook.global.config;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsername("testuser")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("testuser")
                                .password(passwordEncoder.encode("password1234"))
                                .email("testuser@example.com")
                                .build()
                ));

        userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("1q2w3e4r"))
                                .email("admin@example.com")
                                .role(Role.ADMIN)
                                .build()
                ));

        if (productRepository.count() > 0) {
            return;
        }

        Object[][] items = {
                {"2000리갈패드 A4 노랑 60매", 2500, Category.STATIONERY},
                {"단국대 멀티비타민", 120000, Category.HEALTH_FOOD},
                {"단국대 프로틴 멀티비타민 올인원 (90정)", 90000, Category.HEALTH_FOOD},
                {"단국대 라이프어드밴스 올인원 30병입 박스", 540000, Category.HEALTH_FOOD},
                {"블루투스 이어폰", 18000, Category.PERIPHERAL_DEVICE},
                {"단국대 타월(DKU 로고)", 5900, Category.BATHROOM_SUPPLIES},
                {"텀블러 세트", 16500, Category.FASHION_ACCESSORY},
                {"수저세트", 8500, Category.KITCHENWARE},
                {"에코로지 도시락포크(4P)", 16000, Category.KITCHENWARE},
                {"단국대 에코백", 8000, Category.WOMEN_BAG},
                {"단국대 골프공 티세트(6구)", 42000, Category.GOLF},
                {"DKU 스포츠 마사지크림", 23000, Category.HIKING},
                {"단국대 여행캐리어 3종세트", 16000, Category.TRAVEL_BAG_ACCESSORY},
                {"핑크솔트 블랙페퍼 그라인더 세트", 22000, Category.SEASONING},
                {"단국대 PGA 지갑 세트", 20000, Category.TRAVEL_BAG_ACCESSORY},
                {"단국대 박물관 메모지", 15000, Category.STATIONERY},
                {"단국대학교 노트북파우치", 5000, Category.TRAVEL_BAG_ACCESSORY},
                {"단국대 리유저블 텀블러", 4900, Category.KITCHENWARE},
                {"단국대 명찰 목걸이", 3000, Category.FASHION_ACCESSORY},
                {"단국대 벽시계", 35000, Category.HOME_DECOR},
                {"단국대 보행자 네임택", 35000, Category.TRAVEL_BAG_ACCESSORY},
                {"단국대 미니 에코백", 5000, Category.WOMEN_BAG},
                {"단국대 우산", 25000, Category.FASHION_ACCESSORY},
                {"단국대 캠퍼스 토트백", 88000, Category.WOMEN_BAG},
                {"단국대 엽서", 1000, Category.STATIONERY},
                {"단국대 스티커", 2500, Category.STATIONERY},
                {"단국대 가죽 여권통", 9900, Category.STATIONERY},
                {"단국대 앞치마", 10000, Category.KITCHENWARE},
                {"단국대 뱃지", 3000, Category.FASHION_ACCESSORY},
                {"보조배터리(리뉴얼)", 14000, Category.PHONE_ACCESSORY},
                {"아크릴 마그넷", 3000, Category.INTERIOR_ACCESSORY},
                {"호텔 키링", 9000, Category.FASHION_ACCESSORY},
                {"단국대 문진", 45000, Category.INTERIOR_ACCESSORY},
                {"말랑쫀득 로얄젤리", 1200, Category.HEALTH_FOOD},
                {"단국대 2026 달력", 6000, Category.STATIONERY},
                {"단국대 부채", 1000, Category.FASHION_ACCESSORY},
                {"단국대 메쉬파우치", 7900, Category.TRAVEL_BAG_ACCESSORY},
                {"단국대 레터링 명찰목걸이", 5000, Category.FASHION_ACCESSORY},
                {"단국대 타월", 5900, Category.BATHROOM_SUPPLIES},
                {"단국대 아크릴키링", 5500, Category.FASHION_ACCESSORY},
                {"단국대 텀블러 마그넷", 4000, Category.INTERIOR_ACCESSORY},
                {"단국대 양말", 4500, Category.SOCKS},
                {"단국대 샤워용 목욕타월", 6900, Category.BATHROOM_SUPPLIES},
                {"단국대 아크릴 네임택", 15000, Category.TRAVEL_BAG_ACCESSORY},
                {"단국대 벽시계(캠퍼스맵)", 38000, Category.HOME_DECOR},
                {"단국대 머그컵(캠퍼스맵)", 8000, Category.KITCHENWARE},
                {"단국대 인형키링 (MZ/AZ)", 8000, Category.FASHION_ACCESSORY},
                {"단국대 핸드로션 휴대용기", 34800, Category.SEASONAL_APPLIANCE},
                {"찻잔 (한쌍)", 4000, Category.KITCHENWARE},
                {"단국대 커스텀 스티커", 2200, Category.STATIONERY},
        };

        for (Object[] item : items) {
            productRepository.save(
                    Product.builder()
                            .title((String) item[0])
                            .description((String) item[0] + " 판매합니다. 직거래 또는 택배 거래 가능합니다.")
                            .price((Integer) item[1])
                            .stockQuantity(10)
                            .mainCategory(((Category) item[2]).getMainCategory())
                            .category((Category) item[2])
                            .build()
            );
        }
    }
}
