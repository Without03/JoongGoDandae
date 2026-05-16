package com.example.danbook.domain.mypage;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.danbook.domain.product.ProductService;
import com.example.danbook.domain.purchase.PurchaseService;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserService;
import com.example.danbook.domain.wishlist.WishlistService;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 컨트롤러.
 * 로그인한 사용자의 정보, 내 상품 목록, 찜 목록을 표시한다.
 * 상품 수정/삭제는 마이페이지에서만 가능하다 (상세 페이지에서는 불가).
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final ProductService productService;
    private final WishlistService wishlistService;
    private final PurchaseService purchaseService;

    /**
     * 마이페이지 메인.
     * - user: 내 계정 정보 (아이디, 카카오톡 아이디)
     * - myProducts: 내가 등록한 상품 목록 (최신순)
     * - wishlist: 내가 찜한 상품 목록 (최신순)
     */
    @GetMapping
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";

        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);

        model.addAttribute("user", user);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        model.addAttribute("myProducts", isAdmin ? productService.getManagedProducts(username) : java.util.List.of());
        model.addAttribute("wishlist", isAdmin ? java.util.List.of() : wishlistService.getUserWishlist(username));
        model.addAttribute("purchaseHistory", isAdmin ? java.util.List.of() : purchaseService.getUserPurchases(username));
        return "mypage/index";
    }
}
