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
        List<com.foodorderingsystem.model.Order> allOrders = orderService.getAll();
        
        List<com.foodorderingsystem.model.Order> pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .collect(Collectors.toList());
                
        List<com.foodorderingsystem.model.Order> processingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PREPARING || o.getStatus() == OrderStatus.CONFIRMED)
                .collect(Collectors.toList());
                
        List<com.foodorderingsystem.model.Order> shippingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERING)
                .collect(Collectors.toList());
                
        List<com.foodorderingsystem.model.Order> completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.CANCELLED)
                .collect(Collectors.toList());
                
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("processingOrders", processingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("completedOrders", completedOrders);
        
        return "admin/orders";
    }

    @PostMapping("/orders/update-status")
    public String updateStatus(@RequestParam Long orderId, @RequestParam OrderStatus status) {
        orderService.updateStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/api/update-status")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateStatusAjax(@RequestParam Long orderId, @RequestParam OrderStatus status) {
        try {
            orderService.updateStatus(orderId, status);
            return org.springframework.http.ResponseEntity.ok().body("{\"success\":true}");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("{\"success\":false}");
        }
    }
}
