package com.example.danbook.domain.order;

import com.example.danbook.domain.order.dto.CheckoutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) Long productId,
                           @RequestParam(required = false) Integer quantity,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            addCheckoutModel(userDetails.getUsername(), productId, quantity, model);
            CheckoutRequest request = new CheckoutRequest();
            request.setProductId(productId);
            request.setQuantity(quantity == null ? 1 : quantity);
            model.addAttribute("checkoutRequest", request);
            return "order/checkout";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("orderError", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/checkout")
    public String submit(@AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute CheckoutRequest checkoutRequest,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            PurchaseOrder order = orderService.placeOrder(userDetails.getUsername(), checkoutRequest);
            return "redirect:/order/complete?orderId=" + order.getId();
        } catch (IllegalArgumentException e) {
            addCheckoutModel(userDetails.getUsername(), checkoutRequest.getProductId(), checkoutRequest.getQuantity(), model);
            model.addAttribute("checkoutRequest", checkoutRequest);
            model.addAttribute("orderError", e.getMessage());
            return "order/checkout";
        }
    }

    @PostMapping("/buy")
    public String buy(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        return "redirect:/order/checkout";
    }

    @GetMapping("/complete")
    public String complete(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) Long orderId,
                           Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("orderId", orderId);
        return "order/complete";
    }

    private void addCheckoutModel(String username, Long productId, Integer quantity, Model model) {
        var items = orderService.getCheckoutItems(username, productId, quantity);
        int totalPrice = items.stream().mapToInt(item -> item.getLineTotal()).sum();
        model.addAttribute("checkoutItems", items);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("addresses", orderService.getAddresses(username));
        model.addAttribute("paymentMethods", orderService.getPaymentMethods(username));
        model.addAttribute("memoPresets", orderService.getMemoPresets(username));
        model.addAttribute("paymentTypes", PaymentMethodType.values());
    }
}
