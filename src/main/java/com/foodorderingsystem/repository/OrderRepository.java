package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderIdDesc();
    
    List<Order> findByUser_UsernameOrderByOrderDateDesc(String username);

    List<Order> findByOrderDateBetween(Date start, Date end);

    Page<Order> findAll(Pageable pageable);


}
