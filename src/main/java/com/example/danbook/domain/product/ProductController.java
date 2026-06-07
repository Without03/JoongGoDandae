package com.example.danbook.domain.product;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.danbook.domain.product.dto.ProductCreateDto;
import com.example.danbook.domain.product.dto.ProductEditDto;
import com.example.danbook.domain.wishlist.WishlistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 상품 CRUD 및 찜/구매의사 컨트롤러.
 * /products/** 경로를 담당하며, 목록·상세는 비로그인도 접근 가능하다.
 * 등록·수정·삭제·찜·구매의사는 로그인이 필요하다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final WishlistService wishlistService;

    /**
     * 상품 목록 (검색 + 카테고리/메인 카테고리 필터).
     * keyword, category, mainCategory 파라미터 모두 선택값이다.
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) MainCategory mainCategory,
                       @RequestParam(required = false) Category category,
                       @RequestParam(required = false, defaultValue = "popular") String sort,
                       Model model) {
        model.addAttribute("products", productService.searchProducts(keyword, mainCategory, category, sort));
        model.addAttribute("keyword", keyword);
        // products/list.html에서는 현재 사용하지 않는 모델 값이다.
        // model.addAttribute("selectedSort", productService.normalizeSort(sort));
        // model.addAttribute("mainCategories", MainCategory.values());
        // model.addAttribute("selectedMainCategory", mainCategory);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        return "redirect:/";
    }

    /**
     * 상품 상세 페이지.
     * 비로그인 사용자도 접근 가능하며, 로그인 상태에 따라 찜/구매의사 버튼 노출이 다르다.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Product product = productService.getProductById(id);
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;

        model.addAttribute("product", product);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isWishlisted", wishlistService.isWishlisted(id, currentUsername));
        model.addAttribute("wishlistCount", wishlistService.getWishlistCount(id));
        model.addAttribute("images", productService.getProductImages(id));
        return "products/detail";
    }

    /** 상품 등록 폼 (로그인 필요) */
    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (!isAdmin(userDetails)) return "redirect:/";
        ProductCreateDto productCreateDto = new ProductCreateDto();
        productCreateDto.setStockQuantity(10);
        model.addAttribute("productCreateDto", productCreateDto);
        model.addAttribute("mainCategories", MainCategory.values());
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    /**
     * 상품 등록 처리 (로그인 필요).
     * 유효성 오류 시 폼으로 돌아가고, 성공 시 상세 페이지로 리다이렉트.
     */
    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute ProductCreateDto productCreateDto,
                         BindingResult bindingResult,
                         @RequestParam(required = false) List<MultipartFile> images,
                         Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (!isAdmin(userDetails)) return "redirect:/";
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("mainCategories", MainCategory.values());
            model.addAttribute("isEdit", false);
            return "products/form";
        }
        Product product = productService.createProduct(productCreateDto, userDetails.getUsername(), images);
        return "redirect:/products/" + product.getId();
    }

    /**
     * 상품 수정 폼 (관리자만).
     * 관리자가 아니면 상세 페이지로 리다이렉트.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (!isAdmin(userDetails)) return "redirect:/products/" + id;
        Product product = productService.getProductById(id);
        ProductEditDto editDto = new ProductEditDto();
        editDto.setTitle(product.getTitle());
        editDto.setDescription(product.getDescription());
        editDto.setPrice(product.getPrice());
        editDto.setDiscountPrice(product.getDiscountPrice());
        editDto.setStockQuantity(product.getStockQuantity());
        editDto.setMainCategory(product.getMainCategory());
        editDto.setCategory(product.getCategory());
        editDto.setStatus(product.getStatus());
        model.addAttribute("productEditDto", editDto);
        model.addAttribute("mainCategories", MainCategory.values());
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", ProductStatus.values());
        model.addAttribute("productId", id);
        model.addAttribute("existingImages", productService.getProductImages(id));
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    /**
     * 상품 수정 처리 (관리자만).
     * 유효성 오류 또는 권한 오류 시 폼으로 돌아간다.
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute ProductEditDto productEditDto,
                         BindingResult bindingResult,
                         @RequestParam(required = false) List<MultipartFile> images,
                         Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("mainCategories", MainCategory.values());
            model.addAttribute("statuses", ProductStatus.values());
            model.addAttribute("productId", id);
            model.addAttribute("existingImages", productService.getProductImages(id));
            model.addAttribute("isEdit", true);
            return "products/form";
        }
        try {
            productService.updateProduct(id, productEditDto, userDetails.getUsername(), images);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", Category.values());
            model.addAttribute("mainCategories", MainCategory.values());
            model.addAttribute("statuses", ProductStatus.values());
            model.addAttribute("productId", id);
            model.addAttribute("existingImages", productService.getProductImages(id));
            model.addAttribute("isEdit", true);
            return "products/form";
        }
        return "redirect:/products/" + id;
    }

    /**
     * 상품 삭제 (관리자만).
     * 삭제 성공 시 메인으로 리다이렉트.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        try {
            productService.deleteProduct(id, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return "redirect:/products/" + id;
        }
        return "redirect:/";
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    /**
     * 찜 토글 (로그인 필요).
     * 이미 찜한 상품이면 해제, 아니면 찜 추가.
     */
    @PostMapping("/{id}/wishlist")
    public String toggleWishlist(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        if (isAdmin(userDetails)) return "redirect:/products/" + id;
        wishlistService.toggle(id, userDetails.getUsername());
        return "redirect:/products/" + id;
    }

    /**
     * 구매 의사 전달 (로그인 필요).
     * 판매자에게 구매자의 카카오톡 아이디가 포함된 알림을 전송한다.
     * 구매자에게는 판매자 정보를 노출하지 않는다.
     */
    @PostMapping("/{id}/purchase-intent")
    public String purchaseIntent(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        if (isAdmin(userDetails)) return "redirect:/products/" + id;
        return "redirect:/order/checkout?productId=" + id;
    }
}
