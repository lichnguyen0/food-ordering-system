package com.foodorderingsystem.repository.order;

import com.foodorderingsystem.model.order.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    //lấy toàn bộ lịch sử cập nhật của một đơn hàng, sắp xếp theo thời gian cập nhật tăng dần
    List<OrderHistory> findByOrder_OrderIdOrderByUpdateTimeAsc(Long orderId);

    // Đếm số lượng sự kiện theo trạng thái trong khoảng thời gian
    long countByStatusAndUpdateTimeBetween(com.foodorderingsystem.model.order.OrderStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
