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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.danbook.domain.product.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping("/cart")
    public String cartPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("cartItems", cartService.getUserCart(userDetails.getUsername()));
        return "cart/cart";
    }

    @PostMapping("/cart/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> toggle(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            boolean inCart = cartService.toggle(productId, userDetails.getUsername(), quantity);
            return ResponseEntity.ok(Map.of("inCart", inCart));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

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

    @PostMapping("/cart/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        try {
            cartService.updateQuantity(productId, userDetails.getUsername(), quantity);
        } catch (IllegalArgumentException e) {
            model.addAttribute("cartError", e.getMessage());
        }
        model.addAttribute("cartItems", cartService.getUserCart(userDetails.getUsername()));
        return "cart/cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String remove(@PathVariable Long productId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        cartService.remove(productId, userDetails.getUsername());
        return "redirect:/cart";
    }

    @GetMapping("/cart/thumb/{productId}")
    public String thumb(@PathVariable Long productId) {
        var images = productService.getProductImages(productId);
        if (images == null || images.isEmpty()) {
            return "redirect:/images/no-image.png";
        }
        return "redirect:/uploads/" + images.get(0).getFileName();
    }
}
