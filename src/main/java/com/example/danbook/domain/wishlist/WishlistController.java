package com.example.danbook.domain.wishlist;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/wishlist/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> toggle(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean wishlisted = wishlistService.toggle(productId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("wishlisted", wishlisted));
    }

    @GetMapping("/wishlist/status/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> status(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("wishlisted", false));
        }
        boolean wishlisted = wishlistService.isWishlisted(productId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("wishlisted", wishlisted));
    }
}
