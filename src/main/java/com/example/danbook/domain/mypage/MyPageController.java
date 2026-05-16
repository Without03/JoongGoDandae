package com.example.danbook.domain.mypage;

import com.example.danbook.domain.product.ProductService;
import com.example.danbook.domain.purchase.PurchaseService;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserService;
import com.example.danbook.domain.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final ProductService productService;
    private final WishlistService wishlistService;
    private final PurchaseService purchaseService;

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

    @PostMapping("/settings")
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/auth/login";

        try {
            userService.updateUser(userDetails.getUsername(), email, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("settingsSuccess", "설정이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }
}
