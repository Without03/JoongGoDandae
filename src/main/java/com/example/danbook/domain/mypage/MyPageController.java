package com.example.danbook.domain.mypage;

import com.example.danbook.domain.order.OrderService;
import com.example.danbook.domain.order.PaymentMethodType;
import com.example.danbook.domain.order.dto.AddressForm;
import com.example.danbook.domain.order.dto.MemoPresetForm;
import com.example.danbook.domain.order.dto.PaymentMethodForm;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final OrderService orderService;

    @GetMapping
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        model.addAttribute("user", user);
        model.addAttribute("myProducts", isAdmin ? productService.getManagedProducts(username) : java.util.List.of());
        model.addAttribute("wishlist", isAdmin ? java.util.List.of() : wishlistService.getUserWishlist(username));
        model.addAttribute("purchaseHistory", isAdmin ? java.util.List.of() : purchaseService.getUserPurchases(username));
        model.addAttribute("savedAddresses", isAdmin ? java.util.List.of() : orderService.getAddresses(username));
        model.addAttribute("savedPaymentMethods", isAdmin ? java.util.List.of() : orderService.getPaymentMethods(username));
        model.addAttribute("savedMemoPresets", isAdmin ? java.util.List.of() : orderService.getMemoPresets(username));
        model.addAttribute("paymentTypes", PaymentMethodType.values());
        return "mypage/index";
    }

    @PostMapping("/settings")
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            userService.updateUser(userDetails.getUsername(), email, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("settingsSuccess", "계정 설정이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/address")
    public String saveAddress(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute AddressForm form,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.saveAddress(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송지가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/address/{id}/delete")
    public String deleteAddress(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.deleteAddress(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송지가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/payment-method")
    public String savePaymentMethod(@AuthenticationPrincipal UserDetails userDetails,
                                    @ModelAttribute PaymentMethodForm form,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.savePaymentMethod(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "결제수단이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/payment-method/{id}/delete")
    public String deletePaymentMethod(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.deletePaymentMethod(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "결제수단이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/delivery-memo")
    public String saveDeliveryMemo(@AuthenticationPrincipal UserDetails userDetails,
                                   @ModelAttribute MemoPresetForm form,
                                   RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.saveMemoPreset(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송 메모가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }

    @PostMapping("/settings/delivery-memo/{id}/delete")
    public String deleteDeliveryMemo(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.deleteMemoPreset(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송 메모가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage?tab=settings";
    }
}
