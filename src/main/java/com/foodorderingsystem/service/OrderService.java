package com.foodorderingsystem.service;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.Order;

import java.util.List;

public interface OrderService {
    void saveOrder(Cart cart, Long userId);

    List<Order> getAll();
}
