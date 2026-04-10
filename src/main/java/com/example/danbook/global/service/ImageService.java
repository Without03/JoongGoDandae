package com.example.danbook.global.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 파일 1개를 저장하고 저장된 파일명(UUID 기반)을 반환.
     * 빈 파일이면 null 반환.
     */
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        String savedName = UUID.randomUUID() + ext;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            file.transferTo(uploadPath.resolve(savedName).toFile());
            return savedName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + originalName, e);
        }
    }

    public void deleteImage(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
