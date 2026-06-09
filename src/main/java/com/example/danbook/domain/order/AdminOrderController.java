package com.example.danbook.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public String orders(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String scope,
                         Model model) {
        String username = requireUsername(userDetails);
        model.addAttribute("orders", adminOrderService.getOrders(status, scope, username));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedScope", scope);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         Model model) {
        requireUsername(userDetails);
        PurchaseOrder order = adminOrderService.getOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("histories", adminOrderService.getHistories(order));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               @RequestParam OrderStatus status,
                               @RequestParam(required = false) String memo,
                               RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.changeStatus(id, status, memo, requireUsername(userDetails));
            redirectAttributes.addFlashAttribute("adminOrderSuccess", "주문 상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("adminOrderError", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportExcel(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String scope) {
        byte[] workbook = adminOrderService.buildExcel(status, scope, requireUsername(userDetails));
        String filename = "orders-accounting-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(workbook);
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String scope) {
        byte[] csv = adminOrderService.buildCsv(status, scope, requireUsername(userDetails));
        String filename = "orders-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    private String requireUsername(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userDetails.getUsername();
    }
}
