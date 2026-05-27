package com.foodorderingsystem.service;

import com.foodorderingsystem.model.cart.Cart;
import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.order.OrderStatus;
import com.foodorderingsystem.model.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    // ── Core ────────────────────────────────────────────────────────────────
    Order createOrderFromCart(Long userId, Cart cart, com.foodorderingsystem.dto.CheckoutRequest request);
    Order getOrderById(Long orderId);
    List<Order> getOrdersByUsername(String username);
    List<Order> getAll();
    List<Order> getTopRevenue(int n);
    void updateStatus(Long orderId, OrderStatus status);
    long countOrders();
    double calculateTotalRevenue();

    // ── Admin paginated ──────────────────────────────────────────────────────
    Page<Order> getPendingOrders(Pageable pageable);
    Page<Order> getProcessingOrders(Pageable pageable);
    Page<Order> getShippingOrders(Pageable pageable);
    Page<Order> getCompletedOrders(Pageable pageable);
    Page<Order> getAllOrdersPaginated(Pageable pageable);

    // ── Query by status ──────────────────────────────────────────────────────
    /** Trả về danh sách đơn theo 1 trạng thái cụ thể (dùng cho Kitchen, Shipper) */
    List<Order> getOrdersByStatus(OrderStatus status);

    /** Đếm đơn theo trạng thái (dùng cho badge / dashboard count) */
    long countByStatus(OrderStatus status);

    /** Đếm đơn đã xác nhận trong ngày hôm nay */
    long countConfirmedToday();

    /** Đếm đơn bị hủy trong ngày hôm nay */
    long countCancelledToday();

    // ── STAFF actions ────────────────────────────────────────────────────────
    /**
     * STAFF xác nhận đơn: PENDING → CONFIRMED
     * Ghi nhận confirmedAt và log vào OrderHistory.
     */
    void confirmOrder(Long orderId, String staffUsername);

    /**
     * STAFF hủy đơn: PENDING → CANCELLED
     * Ghi nhận lý do hủy.
     */
    void cancelOrderByStaff(Long orderId, String reason);

    // ── KITCHEN actions ──────────────────────────────────────────────────────
    /**
     * KITCHEN bắt đầu chế biến: CONFIRMED → PREPARING
     * Ghi nhận preparingAt.
     */
    void startPreparing(Long orderId);

    /**
     * KITCHEN hoàn tất: PREPARING → READY_FOR_PICKUP
     * Ghi nhận readyAt — đơn sẽ xuất hiện trên dashboard Shipper.
     */
    void markReadyForPickup(Long orderId);

    // ── SHIPPER actions ──────────────────────────────────────────────────────
    /**
     * SHIPPER nhận đơn để giao: READY_FOR_PICKUP → DELIVERING
     * Gắn shipper vào đơn, ghi nhận deliveringAt.
     */
    void pickupOrder(Long orderId, String shipperUsername);

    /**
     * SHIPPER xác nhận giao thành công: DELIVERING → DELIVERED
     * Ghi nhận deliveredAt.
     */
    void completeDelivery(Long orderId);

    /**
     * Lấy danh sách đơn DELIVERING mà shipper này đang giao
     */
    List<Order> getMyDeliveries(String shipperUsername);

    /**
     * Lịch sử giao hàng của shipper (DELIVERED)
     */
    List<Order> getShipperHistory(String shipperUsername);

    /**
     * Lịch sử nấu của bếp
     */
    List<Order> getKitchenHistory(Restaurant restaurant, LocalDateTime start, LocalDateTime end);

    /**
     * Bếp báo hủy đơn đột xuất
     */
    void cancelOrderByKitchen(Long orderId, String reason);

    /**
     * Shipper báo giao thất bại / hủy đơn
     */
    void cancelOrderByShipper(Long orderId, String reason);
}