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
    private final com.foodorderingsystem.repository.CouponRepository couponRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            FoodRepository foodRepository,
                            com.foodorderingsystem.repository.OrderHistoryRepository orderHistoryRepository,
                            com.foodorderingsystem.repository.CouponRepository couponRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.couponRepository = couponRepository;
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
            orderItem.setPrice(item.getUnitPrice());
            orderItem.setOptionsText(item.getOptionsText());

            total += item.getUnitPrice() * item.getQuantity();

            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        // Calculate delivery fee based on first restaurant distance
        Restaurant restaurant = null;
        for (CartItem item : cart.getItems().values()) {
            if (item.getFood() != null && item.getFood().getRestaurant() != null) {
                restaurant = item.getFood().getRestaurant();
                break;
            }
        }

        double deliveryFee = 16000.0;
        if (restaurant != null) {
            order.setRestaurant(restaurant);
            Long rId = restaurant.getRestaurantId();
            double distance = (rId == null) ? 1.5 : (0.5 + (double)(rId % 9) * 0.5);
            deliveryFee = 5000.0 + (distance * 5000.0);
        }
        order.setDeliveryFee(deliveryFee);
        
        double discountAmount = 0.0;
        if (request != null && request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            String cleanCode = request.getCouponCode().trim();
            com.foodorderingsystem.model.Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(cleanCode).orElse(null);
            if (coupon != null && total >= coupon.getMinOrderValue()) {
                if ("PERCENTAGE".equals(coupon.getDiscountType())) {
                    discountAmount = total * (coupon.getDiscountValue() / 100.0);
                    if (coupon.getMaxDiscountAmount() > 0 && discountAmount > coupon.getMaxDiscountAmount()) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                } else if ("FIXED_AMOUNT".equals(coupon.getDiscountType())) {
                    discountAmount = coupon.getDiscountValue();
                }
                
                // Cap discount at subtotal
                if (discountAmount > total) {
                    discountAmount = total;
                }
                
                order.setCouponCode(coupon.getCode());
                order.setDiscountAmount(discountAmount);
                
                // Update coupon use count
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
                    coupon.setActive(false);
                }
                couponRepository.save(coupon);
            }
        }
        
        order.setTotalAmount(Math.max(0.0, total + deliveryFee - discountAmount));

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
        // Exclude cancelled orders when calculating top revenue
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(java.util.stream.Collectors.toList());
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
        // Sum totalAmount for all orders except cancelled ones
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}