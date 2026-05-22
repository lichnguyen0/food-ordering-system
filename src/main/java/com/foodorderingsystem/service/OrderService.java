package com.foodorderingsystem.service;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order createOrderFromCart(Long userId, Cart cart, com.foodorderingsystem.dto.CheckoutRequest request);
    Order getOrderById(Long orderId);
    List<Order> getOrdersByUsername(String username);
    List<Order> getAll();
    List<Order> getTopRevenue(int n);
    void updateStatus(Long orderId, OrderStatus status);
    long countOrders();
    double calculateTotalRevenue();
    
    // Paginated methods for admin orders page
    Page<Order> getPendingOrders(Pageable pageable);
    Page<Order> getProcessingOrders(Pageable pageable);
    Page<Order> getShippingOrders(Pageable pageable);
    Page<Order> getCompletedOrders(Pageable pageable);
}