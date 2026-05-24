package com.foodorderingsystem.model;

public enum OrderStatus {
    PENDING,            // Khách đặt, chờ STAFF xác nhận
    CONFIRMED,          // STAFF đã xác nhận, chờ bếp nhận
    PREPARING,          // KITCHEN đang chế biến
    READY_FOR_PICKUP,   // Bếp xong, chờ SHIPPER đến lấy
    DELIVERING,         // SHIPPER đang trên đường giao
    DELIVERED,          // Đã giao thành công
    COMPLETED,          // Hoàn tất (có thể đánh giá)
    CANCELLED           // Đã hủy
}
