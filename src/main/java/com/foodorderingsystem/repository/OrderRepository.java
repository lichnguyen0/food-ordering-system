package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderIdDesc();
}
