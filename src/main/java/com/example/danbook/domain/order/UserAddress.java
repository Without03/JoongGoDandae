package com.example.danbook.domain.order;

import com.example.danbook.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String label;

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

    @Column(nullable = false)
    private boolean defaultAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserAddress(User user, String label, String recipientName, String phone,
                       String zipcode, String address1, String address2, boolean defaultAddress) {
        this.user = user;
        this.label = label;
        this.recipientName = recipientName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
        this.defaultAddress = defaultAddress;
        this.createdAt = LocalDateTime.now();
    }

    public void clearDefault() {
        this.defaultAddress = false;
    }

    public void update(String label, String recipientName, String phone,
                       String zipcode, String address1, String address2, boolean defaultAddress) {
        this.label = label;
        this.recipientName = recipientName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
        this.defaultAddress = defaultAddress;
    }
}
