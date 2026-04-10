package com.example.danbook.domain.product;

import com.example.danbook.domain.product.dto.ProductCreateDto;
import com.example.danbook.domain.product.dto.ProductEditDto;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import com.example.danbook.global.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public Product createProduct(ProductCreateDto dto, String username, List<MultipartFile> imageFiles) {
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Product product = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .seller(seller)
                .build();

        productRepository.save(product);
        saveImages(product, imageFiles);
        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword, Category category) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        return productRepository.searchProducts(kw, category);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return List.of();
        return productImageRepository.findByProduct(product);
    }

    public void updateProduct(Long id, ProductEditDto dto, String username, List<MultipartFile> imageFiles) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (!product.getSeller().getUsername().equals(username)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        product.update(dto.getTitle(), dto.getDescription(), dto.getPrice(), dto.getCategory(), dto.getStatus());

        // 새 이미지가 있으면 기존 이미지 삭제 후 교체
        boolean hasNewImages = imageFiles != null && imageFiles.stream().anyMatch(f -> !f.isEmpty());
        if (hasNewImages) {
            deleteExistingImages(product);
            saveImages(product, imageFiles);
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getUserProducts(String username) {
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return productRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnails(List<Product> products) {
        Map<Long, String> map = new HashMap<>();
        for (Product p : products) {
            productImageRepository.findFirstByProductOrderByIdAsc(p)
                    .ifPresent(img -> map.put(p.getId(), img.getFileName()));
        }
        return map;
    }

    public void deleteProduct(Long id, String username) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (!product.getSeller().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        deleteExistingImages(product);
        productRepository.delete(product);
    }

    private void saveImages(Product product, List<MultipartFile> imageFiles) {
        if (imageFiles == null) return;
        for (MultipartFile file : imageFiles) {
            String savedName = imageService.saveImage(file);
            if (savedName == null) continue;
            String originalName = file.getOriginalFilename();
            productImageRepository.save(ProductImage.builder()
                    .product(product)
                    .fileName(savedName)
                    .originalName(originalName != null ? originalName : "image")
                    .build());
        }
    }

    private void deleteExistingImages(Product product) {
        List<ProductImage> images = productImageRepository.findByProduct(product);
        images.forEach(img -> imageService.deleteImage(img.getFileName()));
        productImageRepository.deleteByProduct(product);
    }
}
