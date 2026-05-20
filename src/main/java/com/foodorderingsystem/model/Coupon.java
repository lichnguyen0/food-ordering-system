package com.foodorderingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Coupon {// phiếu giảm giá

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Column(nullable = false)
    private String discountType; // "PERCENTAGE" hoặc "FIXED_AMOUNT"

    @Column(nullable = false)
    private double discountValue;

    private double minOrderValue = 0.0;

    private double maxDiscountAmount = 0.0; // Giới hạn động cho chiết khấu phần trăm

    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active = true;

    private int usageLimit = 100;

    private int usedCount = 0;
}
