package com.foodorderingsystem.controller;

import com.foodorderingsystem.dto.CatalogueResponse;
import com.foodorderingsystem.dto.FoodDTO;
import com.foodorderingsystem.mapper.FoodMapper;
import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.model.OrderStatus;
import com.foodorderingsystem.repository.FoodRepository;
import com.foodorderingsystem.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final com.foodorderingsystem.repository.CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    public AdminController(OrderService orderService, FoodRepository foodRepository, FoodMapper foodMapper, com.foodorderingsystem.repository.CategoryRepository categoryRepository) {
        this.orderService = orderService;
        this.foodRepository = foodRepository;
        this.foodMapper = foodMapper;
        this.categoryRepository = categoryRepository;
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
