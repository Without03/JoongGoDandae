package com.example.danbook.domain.order;

import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrders(String statusName, String scope, String adminUsername) {
        if ("mine".equals(scope)) {
            User admin = requireAdmin(adminUsername);
            return historyRepository.findByAdminUserOrderByChangedAtDesc(admin).stream()
                    .map(OrderStatusHistory::getOrder)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .sorted(Comparator.comparing(PurchaseOrder::getOrderedAt).reversed())
                    .toList();
        }

        OrderStatus status = parseStatus(statusName);
        if (status != null) {
            return purchaseOrderRepository.findByStatusOrderByOrderedAtDesc(status);
        }
        return purchaseOrderRepository.findAllByOrderByOrderedAtDesc();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrder(Long orderId) {
        return purchaseOrderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getHistories(PurchaseOrder order) {
        return historyRepository.findByOrderOrderByChangedAtDesc(order);
    }

    public void changeStatus(Long orderId, OrderStatus nextStatus, String memo, String adminUsername) {
        User admin = requireAdmin(adminUsername);
        PurchaseOrder order = getOrder(orderId);
        OrderStatus previousStatus = order.getStatus();
        if (previousStatus == nextStatus) {
            return;
        }
        order.changeStatus(nextStatus);
        historyRepository.save(OrderStatusHistory.builder()
                .order(order)
                .adminUser(admin)
                .fromStatus(previousStatus)
                .toStatus(nextStatus)
                .memo(trimToNull(memo))
                .build());
    }

    @Transactional(readOnly = true)
    public byte[] buildCsv(String statusName, String scope, String adminUsername) {
        List<PurchaseOrder> orders = getOrders(statusName, scope, adminUsername);
        StringBuilder csv = new StringBuilder();
        csv.append("주문번호,주문일시,구매자ID,이메일,수령인,전화번호,주소,상품명,수량,단가,상품합계,총주문금액,결제수단,주문상태\n");
        for (PurchaseOrder order : orders) {
            for (PurchaseOrderItem item : order.getItems()) {
                appendCsvRow(csv,
                        "ORD-" + order.getId(),
                        format(order.getOrderedAt()),
                        order.getOrdererUsername(),
                        order.getOrdererEmail(),
                        order.getRecipientName(),
                        order.getPhone(),
                        order.getZipcode() + " " + order.getAddress1() + " " + order.getAddress2(),
                        item.getProductTitle(),
                        String.valueOf(item.getQuantity()),
                        String.valueOf(item.getUnitPrice()),
                        String.valueOf(item.getLineTotal()),
                        String.valueOf(order.getTotalPrice()),
                        order.getPaymentMethodLabel(),
                        order.getStatus().getDisplayName()
                );
            }
        }
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    private User requireAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }
        return user;
    }

    private OrderStatus parseStatus(String statusName) {
        if (statusName == null || statusName.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void appendCsvRow(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(csvEscape(values[i]));
        }
        csv.append('\n');
    }

    private String csvEscape(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
