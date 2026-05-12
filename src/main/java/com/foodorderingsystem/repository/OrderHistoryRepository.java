package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByOrder_OrderIdOrderByUpdateTimeAsc(Long orderId);
}
