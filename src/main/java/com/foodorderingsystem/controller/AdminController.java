package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.OrderStatus;
import com.foodorderingsystem.repository.FoodRepository;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final FoodRepository foodRepository;

    public AdminController(OrderService orderService, FoodRepository foodRepository) {
        this.orderService = orderService;
        this.foodRepository = foodRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalOrders", orderService.countOrders());
        model.addAttribute("totalRevenue", orderService.calculateTotalRevenue());
        model.addAttribute("totalFood", foodRepository.count());
        model.addAttribute("recentOrders", orderService.getTopRevenue(5));
        return "admin/index";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAll());
        return "admin/orders";
    }

    @PostMapping("/orders/update-status")
    public String updateStatus(@RequestParam Long orderId, @RequestParam OrderStatus status) {
        orderService.updateStatus(orderId, status);
        return "redirect:/admin/orders";
    }
}
