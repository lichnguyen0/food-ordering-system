package com.foodorderingsystem.model.user;

public enum UserRole {
    ADMIN,      // Quản trị toàn hệ thống
    STAFF,      // Nhân viên xác nhận & xử lý đơn hàng
    KITCHEN,    // Nhân viên bếp — chế biến món ăn
    SHIPPER,    // Nhân viên giao hàng
    USER        // Khách hàng
}
