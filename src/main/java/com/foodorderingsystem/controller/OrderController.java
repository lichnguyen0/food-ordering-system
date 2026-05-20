package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes("cart")
public class OrderController {

    private final OrderService orderService;
    private final com.foodorderingsystem.repository.UserRepository userRepository;
    private final com.foodorderingsystem.repository.CouponRepository couponRepository;

    public OrderController(OrderService orderService, 
                           com.foodorderingsystem.repository.UserRepository userRepository,
                           com.foodorderingsystem.repository.CouponRepository couponRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, @ModelAttribute("cart") Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/"; // Redirect to home if cart is empty
        }
        
        double deliveryFee = 16000.0;
        double distance = 1.5;
        int deliveryTime = 25;
        com.foodorderingsystem.model.CartItem firstItem = cart.getItems().values().iterator().next();
        if (firstItem.getFood() != null && firstItem.getFood().getRestaurant() != null) {
            Long rId = firstItem.getFood().getRestaurant().getRestaurantId();
            distance = (rId == null) ? 1.5 : (0.5 + (double)(rId % 9) * 0.5);
            deliveryTime = (rId == null) ? 25 : (15 + (int)(rId % 6) * 5);
            deliveryFee = 5000.0 + (distance * 5000.0);
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("deliveryDistance", distance);
        model.addAttribute("deliveryTime", deliveryTime);
        model.addAttribute("activeCoupons", couponRepository.findAllByActiveTrue());
        return "order/checkout";
    }

    @PostMapping("/order/checkout")
    public String checkout(@ModelAttribute("cart") Cart cart, 
                           @ModelAttribute com.foodorderingsystem.dto.CheckoutRequest request,
                           java.security.Principal principal,
                           SessionStatus status) {

        Long userId = 1L; // Fallback
        if (principal != null) {
            com.foodorderingsystem.model.User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                userId = user.getUserId();
            }
        }

        com.foodorderingsystem.model.Order order = orderService.createOrderFromCart(userId, cart, request);
        
        // Mark session as complete to clear the cart
        status.setComplete();

        if (order != null) {
            return "redirect:/order/tracking/" + order.getOrderId();
        }

        return "redirect:/";
    }

    @GetMapping("/orders")
    public String list(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        java.util.List<com.foodorderingsystem.model.Order> userOrders = orderService.getOrdersByUsername(username);

        java.util.List<com.foodorderingsystem.model.Order> activeOrders = userOrders.stream()
                .filter(o -> {
                    String status = o.getStatus().name();
                    return status.equals("PENDING") || status.equals("CONFIRMED") || status.equals("PREPARING") || status.equals("DELIVERING");
                })
                .collect(java.util.stream.Collectors.toList());

        java.util.List<com.foodorderingsystem.model.Order> historyOrders = userOrders.stream()
                .filter(o -> {
                    String status = o.getStatus().name();
                    return status.equals("DELIVERED") || status.equals("CANCELLED");
                })
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("historyOrders", historyOrders);
        model.addAttribute("allOrders", userOrders);
        return "order/order";
    }

    @GetMapping("/order/tracking/{id}")
    public String trackingPage(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        com.foodorderingsystem.model.Order order = orderService.getOrderById(id);
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        return "order/order-tracking";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@org.springframework.web.bind.annotation.PathVariable("id") Long id, 
                              java.security.Principal principal, 
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        com.foodorderingsystem.model.Order order = orderService.getOrderById(id);
        if (order != null && order.getStatus() == com.foodorderingsystem.model.OrderStatus.PENDING 
                && order.getUser().getUsername().equals(principal.getName())) {
            orderService.updateStatus(id, com.foodorderingsystem.model.OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng này!");
        }
        return "redirect:/orders";
    }

    @PostMapping("/order/reorder/{id}")
    public String reorder(@org.springframework.web.bind.annotation.PathVariable("id") Long id, 
                              jakarta.servlet.http.HttpSession session, 
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        com.foodorderingsystem.model.Order order = orderService.getOrderById(id);
        if (order != null) {
            com.foodorderingsystem.model.Cart cart = (com.foodorderingsystem.model.Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new com.foodorderingsystem.model.Cart();
                session.setAttribute("cart", cart);
            }
            
            for (com.foodorderingsystem.model.OrderItem item : order.getOrderItems()) {
                com.foodorderingsystem.model.Food food = item.getFood();
                int quantity = item.getQuantity();
                for (int i = 0; i < quantity; i++) {
                    cart.add(food);
                }
            }
            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm các món vào giỏ hàng!");
            return "redirect:/checkout";
        }
        return "redirect:/orders";
    }
}
