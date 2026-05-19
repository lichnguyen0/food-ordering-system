package com.foodorderingsystem.service;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderItem;
import com.foodorderingsystem.model.OrderStatus;
import org.springframework.data.domain.Page;

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
}
