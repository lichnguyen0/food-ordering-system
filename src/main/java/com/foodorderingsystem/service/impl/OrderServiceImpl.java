package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.*;
import com.foodorderingsystem.repository.FoodRepository;
import com.foodorderingsystem.repository.OrderItemRepository;
import com.foodorderingsystem.repository.OrderRepository;
import com.foodorderingsystem.repository.UserRepository;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final com.foodorderingsystem.repository.OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            FoodRepository foodRepository,
                            com.foodorderingsystem.repository.OrderHistoryRepository orderHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(Long userId, Cart cart, com.foodorderingsystem.dto.CheckoutRequest request) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        if (request != null) {
            String fullAddress = request.getAddress() != null ? request.getAddress() : "";
            if (request.getDetailAddress() != null && !request.getDetailAddress().isEmpty()) {
                fullAddress = request.getDetailAddress() + ", " + fullAddress;
            }
            order.setDeliveryAddress(fullAddress);
            order.setDeliveryNote(request.getNote());
            order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        }

        order = orderRepository.save(order);

        // Log initial PENDING status
        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.PENDING, LocalDateTime.now()));

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem item : cart.getItems().values()) {

            Food food = item.getFood();
            if (food == null) {
                continue;
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(food.getActivePrice());

            total += food.getActivePrice() * item.getQuantity();

            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        orderRepository.save(order);

        cart.getItems().clear();
        return order;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAllByOrderByOrderIdDesc();
    }

    @Override
    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUser_UsernameOrderByOrderDateDesc(username);
    }

    @Override
    public List<Order> getTopRevenue(int n) {
        List<Order> orders = orderRepository.findAll();
        orders.sort((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));
        if (n > orders.size()) n = orders.size();
        return orders.subList(0, n);
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        
        // Log the status change
        orderHistoryRepository.save(new OrderHistory(order, status, LocalDateTime.now()));
    }

    @Override
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    public double calculateTotalRevenue() {
        return orderRepository.findAll().stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}