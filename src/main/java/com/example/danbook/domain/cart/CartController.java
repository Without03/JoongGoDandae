package com.example.danbook.domain.cart;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.danbook.domain.product.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    /** 장바구니 페이지 */
    @GetMapping("/cart")
    public String cartPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/auth/login";
        model.addAttribute("cartItems", cartService.getUserCart(userDetails.getUsername()));
        return "cart/cart";
    }

    /** 장바구니 토글 (AJAX POST) */
    @PostMapping("/cart/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> toggle(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean inCart = cartService.toggle(productId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("inCart", inCart));
    }

    /** 장바구니 담김 여부 조회 (AJAX GET) */
    @GetMapping("/cart/status/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> status(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("inCart", false));
        }
        boolean inCart = cartService.isInCart(productId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("inCart", inCart));
    }

    /** 장바구니에서 상품 제거 */
    @PostMapping("/cart/remove/{productId}")
    public String remove(@PathVariable Long productId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/auth/login";
        cartService.remove(productId, userDetails.getUsername());
        return "redirect:/cart";
    }

    /** 장바구니 페이지 썸네일 이미지 리다이렉트 */
    @GetMapping("/cart/thumb/{productId}")
    public String thumb(@PathVariable Long productId) {
        var images = productService.getProductImages(productId);
        if (images == null || images.isEmpty()) return "redirect:/images/no-image.png";
        return "redirect:/uploads/" + images.get(0).getFileName();
    }
}