package com.foodorderingsystem.repository.order;

import com.foodorderingsystem.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
