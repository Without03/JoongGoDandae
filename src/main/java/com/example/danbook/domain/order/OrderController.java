package com.example.danbook.domain.order;

import com.example.danbook.domain.cart.CartService;
import com.example.danbook.domain.purchase.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final PurchaseService purchaseService;

    /** 구매 버튼 클릭 → 장바구니 상품 전부 구매 내역에 저장 → 완료 페이지 */
    @PostMapping("/buy")
    public String buy(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";

        String username = userDetails.getUsername();
        cartService.getUserCart(username).forEach(item ->
            purchaseService.recordPurchase(item.getProduct().getId(), username)
        );
        cartService.clearCart(username);

        return "redirect:/order/complete";
    }

    @GetMapping("/complete")
    public String complete(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        return "order/complete";
    }
}