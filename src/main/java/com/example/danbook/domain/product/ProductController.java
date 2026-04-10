package com.example.danbook.domain.product;

import com.example.danbook.domain.notification.NotificationService;
import com.example.danbook.domain.product.dto.ProductCreateDto;
import com.example.danbook.domain.product.dto.ProductEditDto;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserService;
import com.example.danbook.domain.wishlist.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final WishlistService wishlistService;
    private final UserService userService;
    private final NotificationService notificationService;

    // 상품 목록 (검색/필터 포함)
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Category category,
                       Model model) {
        model.addAttribute("products", productService.searchProducts(keyword, category));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        return "products/list";
    }

    // 상품 상세
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

    // 상품 등록 폼
    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        model.addAttribute("productCreateDto", new ProductCreateDto());
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    // 상품 등록 처리
    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute ProductCreateDto productCreateDto,
                         BindingResult bindingResult,
                         @RequestParam(required = false) List<MultipartFile> images,
                         Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("isEdit", false);
            return "products/form";
        }
        Product product = productService.createProduct(productCreateDto, userDetails.getUsername(), images);
        return "redirect:/products/" + product.getId();
    }

    // 상품 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        Product product = productService.getProductById(id);
        if (!product.getSeller().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/products/" + id;
        }
        ProductEditDto editDto = new ProductEditDto();
        editDto.setTitle(product.getTitle());
        editDto.setDescription(product.getDescription());
        editDto.setPrice(product.getPrice());
        editDto.setCategory(product.getCategory());
        editDto.setStatus(product.getStatus());
        model.addAttribute("productEditDto", editDto);
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", ProductStatus.values());
        model.addAttribute("productId", id);
        model.addAttribute("existingImages", productService.getProductImages(id));
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    // 상품 수정 처리
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
            model.addAttribute("statuses", ProductStatus.values());
            model.addAttribute("productId", id);
            model.addAttribute("existingImages", productService.getProductImages(id));
            model.addAttribute("isEdit", true);
            return "products/form";
        }
        return "redirect:/products/" + id;
    }

    // 상품 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        try {
            productService.deleteProduct(id, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return "redirect:/products/" + id;
        }
        return "redirect:/products";
    }

    // 찜 토글
    @PostMapping("/{id}/wishlist")
    public String toggleWishlist(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        wishlistService.toggle(id, userDetails.getUsername());
        return "redirect:/products/" + id;
    }

    // 구매 의사 → 판매자에게 알림 전송, 구매자에게는 판매자 카카오ID 공개
    @PostMapping("/{id}/purchase-intent")
    public String purchaseIntent(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        Product product = productService.getProductById(id);
        if (product.getSeller().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/products/" + id;
        }
        notificationService.createPurchaseNotification(id, userDetails.getUsername());
        model.addAttribute("product", product);
        return "products/contact";
    }
}
