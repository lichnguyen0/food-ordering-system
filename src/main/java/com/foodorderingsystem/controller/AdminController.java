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
    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    public AdminController(OrderService orderService, FoodRepository foodRepository, FoodMapper foodMapper) {
        this.orderService = orderService;
        this.foodRepository = foodRepository;
        this.foodMapper = foodMapper;
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

    @GetMapping("/categories")
    public String categories(Model model) {
        Page<Food> foodPage = foodRepository.findAll(PageRequest.of(0, 6));
        model.addAttribute("foods", foodPage.getContent());
        model.addAttribute("hasMore", foodPage.hasNext());
        model.addAttribute("currentPage", 0);
        return "admin/categories";
    }

    @GetMapping("/categories/load-more")
    @ResponseBody
    public CatalogueResponse loadMore(@RequestParam(defaultValue = "0") int page) {
        Page<Food> foodPage = foodRepository.findAll(PageRequest.of(page, 6));
        List<FoodDTO> dtos = foodPage.getContent().stream()
                .map(foodMapper::toDTO)
                .collect(Collectors.toList());
        return new CatalogueResponse(dtos, foodPage.hasNext(), page);
    }

    @PostMapping("/orders/update-status")

    public String updateStatus(@RequestParam Long orderId, @RequestParam OrderStatus status) {
        orderService.updateStatus(orderId, status);
        return "redirect:/admin/orders";
    }
}
