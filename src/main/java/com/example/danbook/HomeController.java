package com.example.danbook;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

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
