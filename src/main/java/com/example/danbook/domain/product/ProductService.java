package com.example.danbook.domain.product;

import com.example.danbook.domain.product.dto.ProductCreateDto;
import com.example.danbook.domain.product.dto.ProductEditDto;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.UserRepository;
import com.example.danbook.global.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 관련 비즈니스 로직 서비스.
 * 상품 CRUD, 이미지 저장/삭제, 썸네일 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    /**
     * 상품 등록.
     * 이미지 파일 목록이 있으면 함께 저장한다.
     */
    public Product createProduct(ProductCreateDto dto, String username, List<MultipartFile> imageFiles) {
        requireAdmin(username);
        validateCategory(dto.getMainCategory(), dto.getCategory());

        Product product = Product.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .mainCategory(dto.getMainCategory())
                .category(dto.getCategory())
                .build();

        productRepository.save(product);
        saveImages(product, imageFiles);
        return product;
    }

    /**
     * 상품 검색 (키워드 + 카테고리/메인 카테고리 필터, 최신순).
     * keyword가 null이면 전체 조회, category가 null이면 전체 카테고리.
     * mainCategory가 null이 아니면 메인 카테고리로 시작하는 서브 카테고리들만 필터.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword, MainCategory mainCategory, Category category) {
        return searchProducts(keyword, mainCategory, category, "popular");
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword, MainCategory mainCategory, Category category, String sort) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        String selectedSort = normalizeSort(sort);

        return switch (selectedSort) {
            case "recent" -> productRepository.searchProductsOrderByRecent(kw, mainCategory, category);
            case "priceAsc" -> productRepository.searchProductsOrderByPriceAsc(kw, mainCategory, category);
            case "priceDesc" -> productRepository.searchProductsOrderByPriceDesc(kw, mainCategory, category);
            default -> productRepository.searchProductsOrderByPopularity(kw, mainCategory, category);
        };
    }

    public String normalizeSort(String sort) {
        if ("recent".equals(sort) || "priceAsc".equals(sort) || "priceDesc".equals(sort)) {
            return sort;
        }
        return "popular";
    }

    /**
     * 상품 단건 조회.
     * 존재하지 않으면 IllegalArgumentException 발생.
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    }

    /**
     * 특정 상품의 이미지 목록 조회 (상세 페이지 슬라이더용).
     */
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return List.of();
        return productImageRepository.findByProduct(product);
    }

    /**
     * 상품 목록에서 각 상품의 대표 썸네일(첫 번째 이미지)을 한 번에 조회.
     * Map&lt;productId, fileName&gt; 형태로 반환한다.
     */
    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnails(List<Product> products) {
        Map<Long, String> map = new HashMap<>();
        for (Product p : products) {
            productImageRepository.findFirstByProductOrderByIdAsc(p)
                    .ifPresent(img -> map.put(p.getId(), img.getFileName()));
        }
        return map;
    }

    /**
     * 상품 정보 수정.
     * 본인 상품이 아니면 IllegalArgumentException 발생.
     * 새 이미지가 있으면 기존 이미지를 모두 삭제하고 교체한다.
     */
    public void updateProduct(Long id, ProductEditDto dto, String username, List<MultipartFile> imageFiles) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        requireAdmin(username);
        validateCategory(dto.getMainCategory(), dto.getCategory());

        product.update(dto.getTitle(), dto.getDescription(), dto.getPrice(), dto.getMainCategory(), dto.getCategory(), dto.getStatus());

        // 새 이미지가 있으면 기존 이미지 삭제 후 교체
        boolean hasNewImages = imageFiles != null && imageFiles.stream().anyMatch(f -> !f.isEmpty());
        if (hasNewImages) {
            deleteExistingImages(product);
            saveImages(product, imageFiles);
        }
    }

    /**
     * 내가 등록한 상품 목록 조회 (마이페이지, 최신순).
     */
    @Transactional(readOnly = true)
    public List<Product> getManagedProducts(String username) {
        requireAdmin(username);
        return productRepository.findAll();
    }

    /**
     * 상품 삭제.
     * 본인 상품이 아니면 IllegalArgumentException 발생.
     * 이미지 파일도 함께 삭제한다.
     */
    public void deleteProduct(Long id, String username) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        requireAdmin(username);

        deleteExistingImages(product);
        productRepository.delete(product);
    }

    /** 이미지 파일 목록을 저장하고 ProductImage 엔티티로 기록한다. */
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

    /** 상품에 연결된 이미지 DB 레코드와 실제 파일을 모두 삭제한다. */
    private void deleteExistingImages(Product product) {
        List<ProductImage> images = productImageRepository.findByProduct(product);
        images.forEach(img -> imageService.deleteImage(img.getFileName()));
        productImageRepository.deleteByProduct(product);
    }

    private void requireAdmin(String username) {
        boolean isAdmin = userRepository.findByUsername(username)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
        if (!isAdmin) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }
    }

    private void validateCategory(MainCategory mainCategory, Category category) {
        if (mainCategory == null || category == null || category.getMainCategory() != mainCategory) {
            throw new IllegalArgumentException("대분류와 하위분류가 일치하지 않습니다.");
        }
    }
}
