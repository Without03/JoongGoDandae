package com.example.danbook;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.MainCategory;
import com.example.danbook.domain.product.ProductService;
import com.example.danbook.domain.product.ProductStatus;

import lombok.RequiredArgsConstructor;

/**
 * 홈페이지(메인) 컨트롤러. 상품 목록 검색 기능을 제공한다. 비로그인 시 로그인 페이지로 리다이렉트한다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    /**
     * 홈(메인) 페이지. 상품 목록을 검색/필터링하여 표시한다. 로그인 상태에 따라 UI가 다르게 표시된다.
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MainCategory mainCategory,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, defaultValue = "popular") String sort,
            @RequestParam(required = false) String status,
            Model model) {
        var products = productService.searchProducts(keyword, mainCategory, category, sort);

        if ("DISCOUNTED".equals(status)) {
            products = products.stream()
                    .filter(p -> p.getStatus() == ProductStatus.DISCOUNTED)
                    .collect(java.util.stream.Collectors.toList());

        }
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSort", productService.normalizeSort(sort));
        model.addAttribute("mainCategories", MainCategory.values());
        model.addAttribute("selectedMainCategory", mainCategory);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        var discountedProducts = productService.getDiscountedProducts();
        model.addAttribute("discountedProducts", discountedProducts);
        var allForThumbnail = new java.util.ArrayList<>(products);
        discountedProducts.stream()
                .filter(p -> !allForThumbnail.contains(p))
                .forEach(allForThumbnail::add);
        model.addAttribute("thumbnails", productService.getThumbnails(allForThumbnail));
        return "index";
    }
}
