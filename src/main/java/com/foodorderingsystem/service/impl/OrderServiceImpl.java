package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.*;
import com.foodorderingsystem.repository.*;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository        orderRepository;
    private final OrderItemRepository    orderItemRepository;
    private final UserRepository         userRepository;
    private final FoodRepository         foodRepository;
    private final com.foodorderingsystem.repository.CouponRepository couponRepository;
    private final com.foodorderingsystem.service.InvoiceService invoiceService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            FoodRepository foodRepository,
                            OrderHistoryRepository orderHistoryRepository,
                            com.foodorderingsystem.repository.CouponRepository couponRepository,
                            com.foodorderingsystem.service.InvoiceService invoiceService) {
        this.orderRepository        = orderRepository;
        this.orderItemRepository    = orderItemRepository;
        this.userRepository         = userRepository;
        this.foodRepository         = foodRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.couponRepository       = couponRepository;
        this.invoiceService         = invoiceService;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CORE
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public Order createOrderFromCart(Long userId, Cart cart,
                                     com.foodorderingsystem.dto.CheckoutRequest request) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

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
        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.PENDING, LocalDateTime.now()));

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem item : cart.getItems().values()) {
            Food food = item.getFood();
            if (food == null) continue;

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

        // Delivery fee
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

        // Coupon
        double discountAmount = 0.0;
        if (request != null && request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            String cleanCode = request.getCouponCode().trim();
            Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(cleanCode).orElse(null);
            if (coupon != null && total >= coupon.getMinOrderValue()) {
                if ("PERCENTAGE".equals(coupon.getDiscountType())) {
                    discountAmount = total * (coupon.getDiscountValue() / 100.0);
                    if (coupon.getMaxDiscountAmount() > 0 && discountAmount > coupon.getMaxDiscountAmount()) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                } else if ("FIXED_AMOUNT".equals(coupon.getDiscountType())) {
                    discountAmount = coupon.getDiscountValue();
                }
                if (discountAmount > total) discountAmount = total;
                order.setCouponCode(coupon.getCode());
                order.setDiscountAmount(discountAmount);
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                if (coupon.getUsedCount() >= coupon.getUsageLimit()) coupon.setActive(false);
                couponRepository.save(coupon);
            }
        }

        order.setTotalAmount(Math.max(0.0, total + deliveryFee - discountAmount));
        orderRepository.save(order);
        invoiceService.generateInvoiceForOrder(order);
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
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
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
        orderHistoryRepository.save(new OrderHistory(order, status, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, status);
    }

    @Override
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    public double calculateTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADMIN PAGINATED
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Page<Order> getPendingOrders(Pageable pageable) {
        return orderRepository.findByStatus(OrderStatus.PENDING, pageable);
    }

    @Override
    public Page<Order> getProcessingOrders(Pageable pageable) {
        return orderRepository.findByStatusIn(
                List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP), pageable);
    }

    @Override
    public Page<Order> getShippingOrders(Pageable pageable) {
        return orderRepository.findByStatus(OrderStatus.DELIVERING, pageable);
    }

    @Override
    public Page<Order> getCompletedOrders(Pageable pageable) {
        return orderRepository.findByStatusIn(
                List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED, OrderStatus.CANCELLED), pageable);
    }

    @Override
    public Page<Order> getAllOrdersPaginated(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // QUERY BY STATUS
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderDateAsc(status);
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long countCancelledToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);
        return orderRepository.countByStatusAndOrderDateBetween(
                OrderStatus.CANCELLED, startOfDay, endOfDay);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STAFF ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void confirmOrder(Long orderId, String staffUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.PENDING)
            throw new IllegalStateException("Đơn không ở trạng thái PENDING");

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.CONFIRMED, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, OrderStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public void cancelOrderByStaff(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.PENDING)
            throw new IllegalStateException("Chỉ có thể hủy đơn ở trạng thái PENDING");

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.CANCELLED, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, OrderStatus.CANCELLED);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // KITCHEN ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void startPreparing(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.CONFIRMED)
            throw new IllegalStateException("Đơn phải ở trạng thái CONFIRMED");

        order.setStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.PREPARING, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void markReadyForPickup(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.PREPARING)
            throw new IllegalStateException("Đơn phải ở trạng thái PREPARING");

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setReadyAt(LocalDateTime.now());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.READY_FOR_PICKUP, LocalDateTime.now()));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SHIPPER ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void pickupOrder(Long orderId, String shipperUsername) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP)
            throw new IllegalStateException("Đơn phải ở trạng thái READY_FOR_PICKUP");

        User shipper = userRepository.findByUsername(shipperUsername).orElse(null);

        order.setStatus(OrderStatus.DELIVERING);
        order.setShipper(shipper);
        order.setDeliveringAt(LocalDateTime.now());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.DELIVERING, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void completeDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.DELIVERING)
            throw new IllegalStateException("Đơn phải ở trạng thái DELIVERING");

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.DELIVERED, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, OrderStatus.DELIVERED);
    }

    @Override
    public List<Order> getMyDeliveries(String shipperUsername) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getShipper() != null
                        && shipperUsername.equals(o.getShipper().getUsername())
                        && o.getStatus() == OrderStatus.DELIVERING)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getShipperHistory(String shipperUsername) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getShipper() != null
                        && shipperUsername.equals(o.getShipper().getUsername())
                        && (o.getStatus() == OrderStatus.DELIVERED
                            || o.getStatus() == OrderStatus.COMPLETED
                            || o.getStatus() == OrderStatus.CANCELLED))
                .sorted(java.util.Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getKitchenHistory(Restaurant restaurant, LocalDateTime start, LocalDateTime end) {
        List<OrderStatus> statuses = List.of(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.DELIVERING,
                OrderStatus.DELIVERED,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
        );
        if (restaurant != null) {
            return orderRepository.findByRestaurantAndStatusInAndOrderDateBetweenOrderByOrderIdDesc(restaurant, statuses, start, end);
        } else {
            return orderRepository.findByStatusInAndOrderDateBetweenOrderByOrderIdDesc(statuses, start, end);
        }
    }

    @Override
    @Transactional
    public void cancelOrderByKitchen(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Bếp chỉ có thể hủy đơn hàng đang chờ hoặc đang nấu.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason("Bếp báo hủy: " + reason);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.CANCELLED, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void cancelOrderByShipper(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException("Shipper chỉ có thể báo thất bại cho đơn hàng ĐANG GIAO.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason("Giao thất bại: " + reason);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(order, OrderStatus.CANCELLED, LocalDateTime.now()));
        invoiceService.updateInvoiceStatusBasedOnOrder(orderId, OrderStatus.CANCELLED);
    }
}