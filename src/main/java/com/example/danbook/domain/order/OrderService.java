package com.example.danbook.domain.order;

import com.example.danbook.domain.cart.Cart;
import com.example.danbook.domain.cart.CartService;
import com.example.danbook.domain.notification.NotificationService;
import com.example.danbook.domain.order.dto.CheckoutRequest;
import com.example.danbook.domain.product.Product;
import com.example.danbook.domain.product.ProductRepository;
import com.example.danbook.domain.product.ProductStatus;
import com.example.danbook.domain.purchase.PurchaseService;
import com.example.danbook.domain.user.Role;
import com.example.danbook.domain.user.User;
import com.example.danbook.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final PurchaseService purchaseService;
    private final NotificationService notificationService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UserDeliveryMemoPresetRepository memoPresetRepository;

    @Transactional(readOnly = true)
    public List<Product> getCheckoutProducts(String username, Long productId) {
        if (productId != null) {
            return List.of(productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다.")));
        }
        return cartService.getUserCart(username).stream()
                .map(Cart::getProduct)
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

    public PurchaseOrder placeOrder(String username, CheckoutRequest request) {
        User user = getUser(username);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자는 주문할 수 없습니다.");
        }

        List<Product> products = getCheckoutProducts(username, request.getProductId());
        if (products.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }
        products.forEach(this::validateOrderable);

        AddressSnapshot address = resolveAddress(user, request);
        PaymentSnapshot payment = resolvePayment(user, request);
        String deliveryMemo = resolveMemo(user, request);
        int totalPrice = products.stream().mapToInt(Product::getPrice).sum();

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

        products.forEach(product -> order.addItem(PurchaseOrderItem.builder()
                .product(product)
                .productTitle(product.getTitle())
                .unitPrice(product.getPrice())
                .quantity(1)
                .build()));

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        products.forEach(product -> {
            purchaseService.recordPurchase(product.getId(), username);
            notificationService.createPurchaseNotification(product.getId(), username);
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

    private void validateOrderable(Product product) {
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            throw new IllegalArgumentException("'" + product.getTitle() + "' 상품은 품절 상태입니다.");
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
