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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, @ModelAttribute("cart") Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/"; // Redirect to home if cart is empty
        }
        model.addAttribute("cart", cart);
        return "checkout";
    }

    @PostMapping("/order/checkout")
    public String checkout(@ModelAttribute("cart") Cart cart, SessionStatus status) {

        orderService.createOrderFromCart(1L, cart);
        
        // Mark session as complete to clear the cart
        status.setComplete();

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String list(Model model) {
        model.addAttribute("orders", orderService.getAll());
        return "order";
    }
}
