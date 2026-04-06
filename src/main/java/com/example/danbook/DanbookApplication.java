package com.example.danbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// 인텔리제이 환경 테스트 04.07 여성제
/*
04.06 회의 정리
단국 스토어의 개선 요구
- 단국스토어는 네이버스토어의 서비스를 이용중인 것으로 보임.
- 이를 개선하고자 하는 것은 네이버스토어보다 더 나은 서비스를 만들겠다는 것.
가능한가? 아님.
우리가 할 수 있는것
1. 프론트 개선
2. 기존에 없던 서비스를 통한 경쟁력

사이드 이펙트 방지와 원활한 소통을 위해 함수명 등 다이어그램을 완성할것

 */
/*
구성해야 할 것
프론트 : 엄태민
-각 페이지(초기 로그인, 회원가입, 메인, 상품 등록/구매, 마이페이지 등)

백엔드 (로직, DB) : 여성제, 정선우
- DB 접근
- enum struct
- 기타 유틸리티
- 회원가입 시 id 중복 확인
- 로그인 시 id pw 진위 여부 확인
- 필터링 (검색시 카테고리+키워드를 구조체로 넘겨 비교)
- 상품 관리 (등록, 판매/구매)

- 아이템 테이블에 imagepath지정은 하나밖에 못함 -> folderpath로 지정, 폴더명은 item_id로 지정할것

실 개발은 중간고사 이후 예정
 */
@SpringBootApplication
public class DanbookApplication {

	public static void main(String[] args) {
		System.out.println("hello world!");
		SpringApplication.run(DanbookApplication.class, args);
	}

}
