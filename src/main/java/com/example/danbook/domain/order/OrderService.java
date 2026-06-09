package com.example.danbook.domain.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.danbook.domain.cart.CartService;
import com.example.danbook.domain.notification.NotificationService;
import com.example.danbook.domain.order.dto.AddressForm;
import com.example.danbook.domain.order.dto.CheckoutItem;
import com.example.danbook.domain.order.dto.CheckoutRequest;
import com.example.danbook.domain.order.dto.MemoPresetForm;
import com.example.danbook.domain.order.dto.PaymentMethodForm;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.product.ProductStatus;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final NotificationService notificationService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UserDeliveryMemoPresetRepository memoPresetRepository;

    @Transactional(readOnly = true)
    public List<CheckoutItem> getCheckoutItems(String username, Long productId, Integer quantity) {
        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
            int requestedQuantity = quantity == null ? 1 : quantity;
            validateOrderable(product, requestedQuantity);
            return List.of(new CheckoutItem(product, requestedQuantity));
        }

        return cartService.getUserCart(username).stream()
                .map(cart -> new CheckoutItem(cart.getProduct(), cart.getQuantity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAddress> getAddresses(String username) {
        User user = getUser(username);
        return userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<UserPaymentMethod> getPaymentMethods(String username) {
        User user = getUser(username);
        return userPaymentMethodRepository.findByUserOrderByDefaultPaymentMethodDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<UserDeliveryMemoPreset> getMemoPresets(String username) {
        User user = getUser(username);
        return memoPresetRepository.findByUserOrderByDefaultMemoDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getUserOrders(String username) {
        User user = getUser(username);
        return purchaseOrderRepository.findByUserOrderByOrderedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getActiveUserOrders(String username) {
        return getUserOrders(username).stream()
                .filter(order -> !order.isCompleted())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getCompletedUserOrders(String username) {
        return getUserOrders(username).stream()
                .filter(PurchaseOrder::isCompleted)
                .toList();
    }

    public void requestCancel(String username, Long orderId) {
        User user = getUser(username);
        PurchaseOrder order = purchaseOrderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 주문만 취소 신청할 수 있습니다.");
        }
        if (!order.canRequestCancel()) {
            throw new IllegalArgumentException("배송이 시작된 주문은 취소 신청할 수 없습니다.");
        }
        order.changeStatus(OrderStatus.CANCEL_REQUESTED);
        notificationService.createOrderCancelRequestNotification(order);
    }

    public void saveAddress(String username, AddressForm form) {
        User user = getUser(username);
        requireText(form.getRecipientName(), "수령인 이름을 입력해주세요.");
        requireText(form.getPhone(), "전화번호를 입력해주세요.");
        requireText(form.getZipcode(), "우편번호를 입력해주세요.");
        requireText(form.getAddress1(), "기본주소를 입력해주세요.");
        requireText(form.getAddress2(), "상세주소를 입력해주세요.");

        boolean makeDefault = form.isDefaultAddress() || !userAddressRepository.existsByUser(user);
        if (makeDefault) {
            userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(user)
                    .forEach(UserAddress::clearDefault);
        }

        if (form.getId() == null) {
            userAddressRepository.save(UserAddress.builder()
                    .user(user)
                    .label(defaultText(form.getLabel(), "배송지"))
                    .recipientName(form.getRecipientName().trim())
                    .phone(form.getPhone().trim())
                    .zipcode(form.getZipcode().trim())
                    .address1(form.getAddress1().trim())
                    .address2(form.getAddress2().trim())
                    .defaultAddress(makeDefault)
                    .build());
            return;
        }

        UserAddress address = userAddressRepository.findByIdAndUser(form.getId(), user)
                .orElseThrow(() -> new IllegalArgumentException("수정할 배송지를 찾을 수 없습니다."));
        address.update(
                defaultText(form.getLabel(), "배송지"),
                form.getRecipientName().trim(),
                form.getPhone().trim(),
                form.getZipcode().trim(),
                form.getAddress1().trim(),
                form.getAddress2().trim(),
                makeDefault
        );
    }

    public void deleteAddress(String username, Long id) {
        User user = getUser(username);
        UserAddress address = userAddressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 배송지를 찾을 수 없습니다."));
        boolean wasDefault = address.isDefaultAddress();
        userAddressRepository.delete(address);

        if (wasDefault) {
            userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(user).stream()
                    .findFirst()
                    .ifPresent(item -> item.update(
                            item.getLabel(),
                            item.getRecipientName(),
                            item.getPhone(),
                            item.getZipcode(),
                            item.getAddress1(),
                            item.getAddress2(),
                            true
                    ));
        }
    }

    public void savePaymentMethod(String username, PaymentMethodForm form) {
        User user = getUser(username);
        if (form.getType() == null) {
            throw new IllegalArgumentException("결제수단을 선택해주세요.");
        }

        String label = defaultText(form.getLabel(), form.getType().getDisplayName());
        boolean makeDefault = form.isDefaultPaymentMethod() || !userPaymentMethodRepository.existsByUser(user);
        if (makeDefault) {
            userPaymentMethodRepository.findByUserOrderByDefaultPaymentMethodDescCreatedAtDesc(user)
                    .forEach(UserPaymentMethod::clearDefault);
        }

        if (form.getId() == null) {
            userPaymentMethodRepository.save(UserPaymentMethod.builder()
                    .user(user)
                    .type(form.getType())
                    .label(label)
                    .defaultPaymentMethod(makeDefault)
                    .build());
            return;
        }

        UserPaymentMethod method = userPaymentMethodRepository.findByIdAndUser(form.getId(), user)
                .orElseThrow(() -> new IllegalArgumentException("수정할 결제수단을 찾을 수 없습니다."));
        method.update(form.getType(), label, makeDefault);
    }

    public void deletePaymentMethod(String username, Long id) {
        User user = getUser(username);
        UserPaymentMethod method = userPaymentMethodRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 결제수단을 찾을 수 없습니다."));
        boolean wasDefault = method.isDefaultPaymentMethod();
        userPaymentMethodRepository.delete(method);

        if (wasDefault) {
            userPaymentMethodRepository.findByUserOrderByDefaultPaymentMethodDescCreatedAtDesc(user).stream()
                    .findFirst()
                    .ifPresent(item -> item.update(item.getType(), item.getLabel(), true));
        }
    }

    public void saveMemoPreset(String username, MemoPresetForm form) {
        User user = getUser(username);
        requireText(form.getMemo(), "배송 메모를 입력해주세요.");

        boolean makeDefault = form.isDefaultMemo()
                || memoPresetRepository.findByUserOrderByDefaultMemoDescCreatedAtDesc(user).isEmpty();
        if (makeDefault) {
            memoPresetRepository.findByUserOrderByDefaultMemoDescCreatedAtDesc(user)
                    .forEach(UserDeliveryMemoPreset::clearDefault);
        }

        if (form.getId() == null) {
            memoPresetRepository.save(UserDeliveryMemoPreset.builder()
                    .user(user)
                    .memo(form.getMemo().trim())
                    .defaultMemo(makeDefault)
                    .build());
            return;
        }

        UserDeliveryMemoPreset preset = memoPresetRepository.findByIdAndUser(form.getId(), user)
                .orElseThrow(() -> new IllegalArgumentException("수정할 배송 메모를 찾을 수 없습니다."));
        preset.update(form.getMemo().trim(), makeDefault);
    }

    public void deleteMemoPreset(String username, Long id) {
        User user = getUser(username);
        UserDeliveryMemoPreset preset = memoPresetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 배송 메모를 찾을 수 없습니다."));
        boolean wasDefault = preset.isDefaultMemo();
        memoPresetRepository.delete(preset);

        if (wasDefault) {
            memoPresetRepository.findByUserOrderByDefaultMemoDescCreatedAtDesc(user).stream()
                    .findFirst()
                    .ifPresent(item -> item.update(item.getMemo(), true));
        }
    }

    public PurchaseOrder placeOrder(String username, CheckoutRequest request) {
        User user = getUser(username);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 주문할 수 없습니다.");
        }

        List<CheckoutItem> items = getCheckoutItems(username, request.getProductId(), request.getQuantity());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }
        items.forEach(item -> validateOrderable(item.getProduct(), item.getQuantity()));

        AddressSnapshot address = resolveAddress(user, request);
        PaymentSnapshot payment = resolvePayment(user, request);
        String deliveryMemo = resolveMemo(user, request);
        int totalPrice = items.stream().mapToInt(CheckoutItem::getLineTotal).sum();

        PurchaseOrder order = PurchaseOrder.builder()
                .user(user)
                .ordererUsername(user.getUsername())
                .ordererEmail(user.getEmail())
                .recipientName(address.recipientName())
                .phone(address.phone())
                .zipcode(address.zipcode())
                .address1(address.address1())
                .address2(address.address2())
                .deliveryMemo(deliveryMemo)
                .paymentMethodType(payment.type())
                .paymentMethodLabel(payment.label())
                .totalPrice(totalPrice)
                .build();

        items.forEach(item -> order.addItem(PurchaseOrderItem.builder()
                .product(item.getProduct())
                .productTitle(item.getProduct().getTitle())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .build()));

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        items.forEach(item -> {
            item.getProduct().decreaseStock(item.getQuantity());
            notificationService.createPurchaseNotification(item.getProduct().getId(), username);
        });

        if (request.getProductId() == null) {
            cartService.clearCart(username);
        }
        return savedOrder;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private void validateOrderable(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("구매 수량은 1개 이상이어야 합니다.");
        }
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("'" + product.getTitle() + "' 상품이 품절 상태입니다.");
        }
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("'" + product.getTitle() + "' 상품의 재고가 부족합니다.");
        }
    }

    private AddressSnapshot resolveAddress(User user, CheckoutRequest request) {
        if (request.getAddressId() != null) {
            UserAddress address = userAddressRepository.findByIdAndUser(request.getAddressId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 배송지를 찾을 수 없습니다."));
            return new AddressSnapshot(address.getRecipientName(), address.getPhone(), address.getZipcode(),
                    address.getAddress1(), address.getAddress2());
        }

        requireText(request.getRecipientName(), "수령인 이름을 입력해주세요.");
        requireText(request.getPhone(), "전화번호를 입력해주세요.");
        requireText(request.getZipcode(), "우편번호를 입력해주세요.");
        requireText(request.getAddress1(), "기본주소를 입력해주세요.");
        requireText(request.getAddress2(), "상세주소를 입력해주세요.");

        boolean makeDefault = request.isDefaultAddress() || !userAddressRepository.existsByUser(user);
        if (request.isSaveAddress()) {
            if (makeDefault) {
                userAddressRepository.findByUserOrderByDefaultAddressDescCreatedAtDesc(user)
                        .forEach(UserAddress::clearDefault);
            }
            userAddressRepository.save(UserAddress.builder()
                    .user(user)
                    .label(defaultText(request.getAddressLabel(), "배송지"))
                    .recipientName(request.getRecipientName().trim())
                    .phone(request.getPhone().trim())
                    .zipcode(request.getZipcode().trim())
                    .address1(request.getAddress1().trim())
                    .address2(request.getAddress2().trim())
                    .defaultAddress(makeDefault)
                    .build());
        }

        return new AddressSnapshot(request.getRecipientName().trim(), request.getPhone().trim(),
                request.getZipcode().trim(), request.getAddress1().trim(), request.getAddress2().trim());
    }

    private PaymentSnapshot resolvePayment(User user, CheckoutRequest request) {
        if (request.getPaymentMethodId() != null) {
            UserPaymentMethod payment = userPaymentMethodRepository.findByIdAndUser(request.getPaymentMethodId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 결제수단을 찾을 수 없습니다."));
            return new PaymentSnapshot(payment.getType(), payment.getLabel());
        }

        if (request.getPaymentMethodType() == null) {
            throw new IllegalArgumentException("결제수단을 선택해주세요.");
        }

        String label = defaultText(request.getPaymentMethodLabel(), request.getPaymentMethodType().getDisplayName());
        boolean makeDefault = request.isDefaultPaymentMethod() || !userPaymentMethodRepository.existsByUser(user);
        if (request.isSavePaymentMethod()) {
            if (makeDefault) {
                userPaymentMethodRepository.findByUserOrderByDefaultPaymentMethodDescCreatedAtDesc(user)
                        .forEach(UserPaymentMethod::clearDefault);
            }
            userPaymentMethodRepository.save(UserPaymentMethod.builder()
                    .user(user)
                    .type(request.getPaymentMethodType())
                    .label(label)
                    .defaultPaymentMethod(makeDefault)
                    .build());
        }

        return new PaymentSnapshot(request.getPaymentMethodType(), label);
    }

    private String resolveMemo(User user, CheckoutRequest request) {
        String memo = trimToNull(request.getDeliveryMemo());
        if (memo == null && request.getMemoPresetId() != null) {
            memo = memoPresetRepository.findByIdAndUser(request.getMemoPresetId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 배송 메모를 찾을 수 없습니다."))
                    .getMemo();
        }

        if (memo != null && request.isSaveDeliveryMemo()) {
            if (request.isDefaultDeliveryMemo()) {
                memoPresetRepository.findByUserOrderByDefaultMemoDescCreatedAtDesc(user)
                        .forEach(UserDeliveryMemoPreset::clearDefault);
            }
            memoPresetRepository.save(UserDeliveryMemoPreset.builder()
                    .user(user)
                    .memo(memo)
                    .defaultMemo(request.isDefaultDeliveryMemo())
                    .build());
        }
        return memo;
    }

    private void requireText(String value, String message) {
        if (trimToNull(value) == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private String defaultText(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record AddressSnapshot(String recipientName, String phone, String zipcode, String address1, String address2) {
    }

    private record PaymentSnapshot(PaymentMethodType type, String label) {
    }
}
