package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByOrderIdDesc();

    List<Order> findByUser_UsernameOrderByOrderDateDesc(String username);

    List<Order> findByOrderDateBetween(Date start, Date end);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // ── Thêm cho Staff / Kitchen / Shipper dashboards ──────────────────────

    /** Lấy danh sách đơn theo 1 trạng thái, sắp xếp theo thời gian đặt cũ nhất trước */
    List<Order> findByStatusOrderByOrderDateAsc(OrderStatus status);

    /** Đếm đơn theo trạng thái (badge counter) */
    long countByStatus(OrderStatus status);

    /** Đếm đơn bị hủy trong khoảng thời gian (thống kê ngày hôm nay) */
    long countByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    List<Order> findByRestaurantAndStatusInAndOrderDateBetweenOrderByOrderIdDesc(
            com.foodorderingsystem.model.Restaurant restaurant,
            List<OrderStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Order> findByStatusInAndOrderDateBetweenOrderByOrderIdDesc(
            List<OrderStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );
}

