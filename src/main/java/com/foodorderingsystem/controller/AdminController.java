package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Order;
import org.springframework.http.ResponseEntity;


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

    /*
     Hiển thị dashboard trang admin.
     Bao gồm:
     - Tổng số đơn hàng
     - Tổng doanh thu
     - Tổng số món ăn
     - Danh sách đơn hàng doanh thu cao gần đây
*/
    @GetMapping
    public String dashboard(Model model) {
        // Tổng số đơn hàng
        model.addAttribute("totalOrders", orderService.countOrders());
        // Tổng doanh thu
        model.addAttribute("totalRevenue", orderService.calculateTotalRevenue());
        // Tổng số món ăn
        model.addAttribute("totalFood", foodRepository.count());
        // Lấy top 5 đơn hàng doanh thu cao
        model.addAttribute("recentOrders", orderService.getTopRevenue(5));

        return "admin/index";
    }

    /**
     * Hiển thị danh sách đơn hàng theo từng trạng thái.
     * Chia thành:
     * - Đơn chờ xác nhận
     * - Đơn đang xử lý
     * - Đơn đang giao
     * - Đơn hoàn thành / đã hủy
     */
    @GetMapping("/orders")
    public String orders(Model model) {

        // Lấy toàn bộ đơn hàng
        List<Order> allOrders = orderService.getAll();

        List<Order> pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .collect(Collectors.toList());

        List<Order> processingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PREPARING
                        || o.getStatus() == OrderStatus.CONFIRMED)
                .collect(Collectors.toList());

        List<Order> shippingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERING)
                .collect(Collectors.toList());

        List<Order> completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED
                        || o.getStatus() == OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("processingOrders", processingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("completedOrders", completedOrders);

        return "admin/orders";
    }

    /**
     * Cập nhật trạng thái đơn hàng bằng form submit thông thường.
     * Sau khi cập nhật sẽ redirect về trang orders.
     */
    @PostMapping("/orders/update-status")
    public String updateStatus(@RequestParam Long orderId, @RequestParam OrderStatus status) {
        orderService.updateStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    /**
     * Cập nhật trạng thái đơn hàng bằng AJAX.
     * Trả về JSON response cho JavaScript xử lý.
     */

    @PostMapping("/orders/api/update-status")
    @ResponseBody
    public ResponseEntity<?> updateStatusAjax(@RequestParam Long orderId,
                                              @RequestParam OrderStatus status) {

        try {

            // Cập nhật trạng thái đơn hàng
            orderService.updateStatus(orderId, status);

            // Trả về success nếu thành công
            return ResponseEntity.ok()
                    .body("{\"success\":true}");

        } catch (Exception e) {

            // Trả về lỗi nếu update thất bại
            return ResponseEntity.badRequest()
                    .body("{\"success\":false}");
        }
    }
}
