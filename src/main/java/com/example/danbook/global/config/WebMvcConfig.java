package com.example.danbook.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 정적 리소스 핸들러 설정.
 * 업로드된 이미지 파일이 저장된 로컬 디렉터리를 /uploads/** URL로 서빙한다.
 * application.properties의 file.upload-dir 값을 기반으로 절대 경로를 계산한다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** application.properties에서 주입, 기본값은 "uploads" */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * /uploads/** 요청을 로컬 uploads/ 디렉터리로 매핑한다.
     * 예: /uploads/abc123.jpg → {uploadDir}/abc123.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
