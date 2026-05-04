package com.example.danbook;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * 홈페이지(메인) 컨트롤러.
 * 상품 목록 검색 기능을 제공한다.
 * 비로그인 시 로그인 페이지로 리다이렉트한다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    /**
     * 홈(메인) 페이지.
     * 상품 목록을 검색/필터링하여 표시한다.
     * - 로그인이 필요하며, 비로그인 시 로그인 페이지로 이동
     * - keyword와 category로 검색/필터링
     * - 검색 결과와 각 상품의 썸네일 이미지를 모델에 추가
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Category category,
                       Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        var products = productService.searchProducts(keyword, category);
        model.addAttribute("products", products);
        model.addAttribute("thumbnails", productService.getThumbnails(products));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        return "index";
    }
}
