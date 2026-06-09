package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String ordererUsername;

    @Column(nullable = false, length = 120)
    private String ordererEmail;

    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 20)
    private String zipcode;

    @Column(nullable = false, length = 200)
    private String address1;

    @Column(nullable = false, length = 200)
    private String address2;

    @Column(length = 200)
    private String deliveryMemo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType paymentMethodType;

    @Column(nullable = false, length = 80)
    private String paymentMethodLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private OrderStatus status;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Builder
    public PurchaseOrder(User user, String ordererUsername, String ordererEmail,
                         String recipientName, String phone, String zipcode,
                         String address1, String address2, String deliveryMemo,
                         PaymentMethodType paymentMethodType, String paymentMethodLabel,
                         int totalPrice) {
        this.user = user;
        this.ordererUsername = ordererUsername;
        this.ordererEmail = ordererEmail;
        this.recipientName = recipientName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
        this.deliveryMemo = deliveryMemo;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodLabel = paymentMethodLabel;
        this.status = OrderStatus.RECEIVED;
        this.totalPrice = totalPrice;
        this.orderedAt = LocalDateTime.now();
    }

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void changeStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("변경할 주문 상태를 선택해주세요.");
        }
        this.status = status;
    }

    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELED;
    }

    public boolean canRequestCancel() {
        return status == OrderStatus.RECEIVED
                || status == OrderStatus.PAYMENT_PENDING
                || status == OrderStatus.PAID
                || status == OrderStatus.PREPARING;
    }

    public String getItemSummary() {
        if (items.isEmpty()) {
            return "-";
        }
        String title = items.get(0).getProductTitle();
        if (items.size() == 1) {
            return title;
        }
        return title + " 외 " + (items.size() - 1) + "건";
    }
}
