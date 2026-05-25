package com.foodorderingsystem.model.payment;

import com.foodorderingsystem.model.order.Order;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(unique = true, nullable = false)
    private String invoiceCode; // e.g. INV-2026-1001

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    private String customerName;
    private String customerPhone;
    
    private double totalAmount;

    @Column(length = 50)
    private String paymentMethod; // Cash, Momo, VNPay...

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
