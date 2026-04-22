package com.example.danbook.domain.cart;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping("/cart")
    public String cartPage() {
        return "cart/cart"; 
    }
}