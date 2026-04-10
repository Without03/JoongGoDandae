package com.example.danbook.domain.mypage;

import com.example.danbook.domain.product.ProductService;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserService;
import com.example.danbook.domain.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final ProductService productService;
    private final WishlistService wishlistService;

    @GetMapping
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";

        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("myProducts", productService.getUserProducts(username));
        model.addAttribute("wishlist", wishlistService.getUserWishlist(username));
        return "mypage/index";
    }
}
