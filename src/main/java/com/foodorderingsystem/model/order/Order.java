package com.foodorderingsystem.model.order;

import com.foodorderingsystem.model.order.OrderHistory;
import com.foodorderingsystem.model.order.OrderItem;
import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("updateTime ASC")
    private List<OrderHistory> history;

    private double totalAmount;
    private Double deliveryFee;

    @Column(length = 255)
    private String deliveryAddress;

    @Column(length = 500)
    private String deliveryNote;

    @Column(length = 50)
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantId")
    private Restaurant restaurant;

    private String couponCode;
    private double discountAmount = 0.0;

    // ── Shipper được giao đơn này ──────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    // ── Timestamps theo từng bước trong luồng đơn hàng ────────────────────
    /** STAFF xác nhận đơn lúc nào */
    private LocalDateTime confirmedAt;

    /** KITCHEN bắt đầu nấu lúc nào */
    private LocalDateTime preparingAt;

    /** KITCHEN hoàn tất, sẵn sàng giao lúc nào */
    private LocalDateTime readyAt;

    /** SHIPPER nhận đơn để giao lúc nào */
    private LocalDateTime deliveringAt;

    /** SHIPPER giao thành công lúc nào */
    private LocalDateTime deliveredAt;

    // ── Lý do hủy đơn ─────────────────────────────────────────────────────
    @Column(length = 500)
    private String cancelReason;
}
