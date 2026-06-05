package com.example.danbook.domain.mypage;

import java.util.List;

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
    public String myPage(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(defaultValue = "profile") String tab,
                         Model model) {
        User user = requireUser(userDetails);
        populateCommonModel(model, user);
        model.addAttribute("activeTab", normalizeMainTab(tab));
        return "mypage/index";
    }

    @GetMapping("/order-settings")
    public String orderSettingsHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = requireUser(userDetails);
        populateCommonModel(model, user);
        populateOrderSettingsModel(model, user);
        model.addAttribute("activeOrderSection", "overview");
        return "mypage/order-settings";
    }

    @GetMapping("/order-settings/addresses")
    public String orderAddresses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = requireUser(userDetails);
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/mypage/order-settings";
        }
        populateCommonModel(model, user);
        populateOrderSettingsModel(model, user);
        model.addAttribute("activeOrderSection", "addresses");
        return "mypage/order-addresses";
    }

    @GetMapping("/order-settings/payment-methods")
    public String orderPaymentMethods(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = requireUser(userDetails);
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/mypage/order-settings";
        }
        populateCommonModel(model, user);
        populateOrderSettingsModel(model, user);
        model.addAttribute("activeOrderSection", "payment-methods");
        return "mypage/order-payment-methods";
    }

    @GetMapping("/order-settings/delivery-memos")
    public String orderDeliveryMemos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = requireUser(userDetails);
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/mypage/order-settings";
        }
        populateCommonModel(model, user);
        populateOrderSettingsModel(model, user);
        model.addAttribute("activeOrderSection", "delivery-memos");
        return "mypage/order-delivery-memos";
    }

    @PostMapping("/settings")
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

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
        requireUser(userDetails);

        try {
            orderService.saveAddress(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송지가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/addresses";
    }

    @PostMapping("/settings/address/{id}/delete")
    public String deleteAddress(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

        try {
            orderService.deleteAddress(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송지가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/addresses";
    }

    @PostMapping("/settings/payment-method")
    public String savePaymentMethod(@AuthenticationPrincipal UserDetails userDetails,
                                    @ModelAttribute PaymentMethodForm form,
                                    RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

        try {
            orderService.savePaymentMethod(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "결제수단이 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/payment-methods";
    }

    @PostMapping("/settings/payment-method/{id}/delete")
    public String deletePaymentMethod(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

        try {
            orderService.deletePaymentMethod(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "결제수단이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/payment-methods";
    }

    @PostMapping("/settings/delivery-memo")
    public String saveDeliveryMemo(@AuthenticationPrincipal UserDetails userDetails,
                                   @ModelAttribute MemoPresetForm form,
                                   RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

        try {
            orderService.saveMemoPreset(userDetails.getUsername(), form);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송 메모가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/delivery-memos";
    }

    @PostMapping("/settings/delivery-memo/{id}/delete")
    public String deleteDeliveryMemo(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        requireUser(userDetails);

        try {
            orderService.deleteMemoPreset(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("settingsSuccess", "배송 메모가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("settingsError", e.getMessage());
        }

        return "redirect:/mypage/order-settings/delivery-memos";
    }

    private User requireUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }
        return userService.getUserByUsername(userDetails.getUsername());
    }

    private void populateCommonModel(Model model, User user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        String username = user.getUsername();

        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("roleLabel", isAdmin ? "관리자" : "일반 사용자");
        model.addAttribute("activeRoot", "mypage");
        model.addAttribute("myProducts", isAdmin ? productService.getManagedProducts(username) : List.of());
        model.addAttribute("wishlist", isAdmin ? List.of() : wishlistService.getUserWishlist(username));
        model.addAttribute("purchaseHistory", isAdmin ? List.of() : purchaseService.getUserPurchases(username));
    }

    private void populateOrderSettingsModel(Model model, User user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        String username = user.getUsername();

        model.addAttribute("savedAddresses", isAdmin ? List.of() : orderService.getAddresses(username));
        model.addAttribute("savedPaymentMethods", isAdmin ? List.of() : orderService.getPaymentMethods(username));
        model.addAttribute("savedMemoPresets", isAdmin ? List.of() : orderService.getMemoPresets(username));
        model.addAttribute("paymentTypes", PaymentMethodType.values());
        model.addAttribute("activeRoot", "order-settings");
    }

    private String normalizeMainTab(String tab) {
        return switch (tab) {
            case "purchase", "wishlist", "settings" -> tab;
            default -> "profile";
        };
    }
}
