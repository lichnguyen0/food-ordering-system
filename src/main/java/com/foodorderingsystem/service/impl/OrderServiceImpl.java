package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.CartItem;
import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderItem;
import com.foodorderingsystem.repository.OrderItemRepository;
import com.foodorderingsystem.repository.OrderRepository;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public void saveOrder(Cart cart, Long userId) {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setOrderDate(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        orderRepository.save(order);

        for (CartItem item : cart.getItems().values()) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFoodId(item.getFoodId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());

            orderItemRepository.save(orderItem);
        }

        cart.getItems().clear();
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAllByOrderByOrderIdDesc();
    }

}
