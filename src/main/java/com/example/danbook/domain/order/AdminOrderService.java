package com.example.danbook.domain.order;

import com.example.danbook.domain.product.Category;
import com.example.danbook.domain.product.MainCategory;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderService {

    private static final String UNPROCESSED_STATUS = "UNPROCESSED";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] ACCOUNTING_EXPORT_HEADERS = {
            "거래일시",
            "주문번호",
            "구매자ID",
            "구매자이메일",
            "적요",
            "상품명",
            "수량",
            "단가",
            "상품합계",
            "총주문금액",
            "결제수단",
            "주문상태",
            "수령인",
            "전화번호",
            "배송주소"
    };

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrders(String statusName, LocalDate startDate, LocalDate endDate, List<String> productTags) {
        List<String> normalizedTags = normalizeTags(productTags);
        return purchaseOrderRepository.findAllByOrderByOrderedAtDesc().stream()
                .filter(order -> matchesStatusFilter(order, statusName))
                .filter(order -> matchesDateRange(order, startDate, endDate))
                .filter(order -> matchesProductTags(order, normalizedTags))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getProductTagOptions() {
        Set<String> options = new LinkedHashSet<>();
        for (PurchaseOrder order : purchaseOrderRepository.findAllByOrderByOrderedAtDesc()) {
            for (PurchaseOrderItem item : order.getItems()) {
                addTagOption(options, item.getProductTitle());
                Product product = item.getProduct();
                if (product != null) {
                    addTagOption(options, product.getTitle());
                }
            }
        }
        for (MainCategory mainCategory : MainCategory.values()) {
            addTagOption(options, mainCategory.getDisplayName());
        }
        for (Category category : Category.values()) {
            addTagOption(options, category.getDisplayName());
        }
        return options.stream().toList();
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
    public byte[] buildCsv(String statusName, LocalDate startDate, LocalDate endDate, List<String> productTags) {
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, ACCOUNTING_EXPORT_HEADERS);
        for (AccountingExportRow row : buildAccountingRows(statusName, startDate, endDate, productTags)) {
            appendCsvRow(csv,
                    row.orderedAt(),
                    row.orderNumber(),
                    row.ordererUsername(),
                    row.ordererEmail(),
                    row.summary(),
                    row.productTitle(),
                    String.valueOf(row.quantity()),
                    String.valueOf(row.unitPrice()),
                    String.valueOf(row.lineTotal()),
                    String.valueOf(row.orderTotal()),
                    row.paymentMethod(),
                    row.orderStatus(),
                    row.recipientName(),
                    row.phone(),
                    row.shippingAddress()
            );
        }
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] buildExcel(String statusName, LocalDate startDate, LocalDate endDate, List<String> productTags) {
        List<AccountingExportRow> rows = buildAccountingRows(statusName, startDate, endDate, productTags);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("주문 정산");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < ACCOUNTING_EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ACCOUNTING_EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (AccountingExportRow exportRow : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(exportRow.orderedAt());
                row.createCell(1).setCellValue(exportRow.orderNumber());
                row.createCell(2).setCellValue(exportRow.ordererUsername());
                row.createCell(3).setCellValue(exportRow.ordererEmail());
                row.createCell(4).setCellValue(exportRow.summary());
                row.createCell(5).setCellValue(exportRow.productTitle());
                row.createCell(6).setCellValue(exportRow.quantity());
                row.createCell(7).setCellValue(exportRow.unitPrice());
                row.createCell(8).setCellValue(exportRow.lineTotal());
                row.createCell(9).setCellValue(exportRow.orderTotal());
                row.createCell(10).setCellValue(exportRow.paymentMethod());
                row.createCell(11).setCellValue(exportRow.orderStatus());
                row.createCell(12).setCellValue(exportRow.recipientName());
                row.createCell(13).setCellValue(exportRow.phone());
                row.createCell(14).setCellValue(exportRow.shippingAddress());
            }

            for (int i = 0; i < ACCOUNTING_EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일을 생성할 수 없습니다.", e);
        }
    }

    private List<AccountingExportRow> buildAccountingRows(String statusName, LocalDate startDate, LocalDate endDate,
                                                          List<String> productTags) {
        List<AccountingExportRow> rows = new ArrayList<>();
        List<String> normalizedTags = normalizeTags(productTags);
        for (PurchaseOrder order : getOrders(statusName, startDate, endDate, productTags)) {
            for (PurchaseOrderItem item : order.getItems()) {
                if (!matchesItemTags(item, normalizedTags)) {
                    continue;
                }
                rows.add(new AccountingExportRow(
                        format(order.getOrderedAt()),
                        "ORD-" + order.getId(),
                        order.getOrdererUsername(),
                        order.getOrdererEmail(),
                        order.getOrdererUsername() + " 주문 " + item.getProductTitle(),
                        item.getProductTitle(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal(),
                        order.getTotalPrice(),
                        order.getPaymentMethodLabel(),
                        order.getStatus().getDisplayName(),
                        order.getRecipientName(),
                        order.getPhone(),
                        order.getZipcode() + " " + order.getAddress1() + " " + order.getAddress2()
                ));
            }
        }
        return rows;
    }

    private User requireAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }
        return user;
    }

    private boolean matchesStatusFilter(PurchaseOrder order, String statusName) {
        if (statusName == null || statusName.isBlank()) {
            return true;
        }
        if (UNPROCESSED_STATUS.equals(statusName)) {
            return order.getStatus() != OrderStatus.DELIVERED
                    && order.getStatus() != OrderStatus.CANCELED;
        }
        try {
            return order.getStatus() == OrderStatus.valueOf(statusName);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private boolean matchesDateRange(PurchaseOrder order, LocalDate startDate, LocalDate endDate) {
        LocalDateTime orderedAt = order.getOrderedAt();
        if (orderedAt == null) {
            return false;
        }
        if (startDate != null && orderedAt.isBefore(startDate.atStartOfDay())) {
            return false;
        }
        return endDate == null || orderedAt.isBefore(endDate.plusDays(1).atStartOfDay());
    }

    private boolean matchesProductTags(PurchaseOrder order, List<String> tags) {
        if (tags.isEmpty()) {
            return true;
        }
        return order.getItems().stream().anyMatch(item -> matchesItemTags(item, tags));
    }

    private boolean matchesItemTags(PurchaseOrderItem item, List<String> tags) {
        if (tags.isEmpty()) {
            return true;
        }
        return tags.stream().allMatch(tag -> matchesTag(item, tag));
    }

    private boolean matchesTag(PurchaseOrderItem item, String tag) {
        Product product = item.getProduct();
        return containsIgnoreCase(item.getProductTitle(), tag)
                || product != null && (
                containsIgnoreCase(product.getTitle(), tag)
                        || matchesMainCategory(product.getMainCategory(), tag)
                        || matchesCategory(product.getCategory(), tag)
        );
    }

    private boolean matchesMainCategory(MainCategory mainCategory, String tag) {
        return mainCategory != null && (
                containsIgnoreCase(mainCategory.name(), tag)
                        || containsIgnoreCase(mainCategory.getDisplayName(), tag)
        );
    }

    private boolean matchesCategory(Category category, String tag) {
        return category != null && (
                containsIgnoreCase(category.name(), tag)
                        || containsIgnoreCase(category.getDisplayName(), tag)
        );
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(this::trimToNull)
                .filter(tag -> tag != null)
                .distinct()
                .toList();
    }

    private void addTagOption(Set<String> options, String value) {
        String tag = trimToNull(value);
        if (tag != null) {
            options.add(tag);
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        if (value == null || keyword == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
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

    private record AccountingExportRow(
            String orderedAt,
            String orderNumber,
            String ordererUsername,
            String ordererEmail,
            String summary,
            String productTitle,
            int quantity,
            int unitPrice,
            int lineTotal,
            int orderTotal,
            String paymentMethod,
            String orderStatus,
            String recipientName,
            String phone,
            String shippingAddress
    ) {
    }
}
