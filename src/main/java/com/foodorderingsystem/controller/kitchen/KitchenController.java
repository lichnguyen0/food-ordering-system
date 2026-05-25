package com.foodorderingsystem.controller.kitchen;

import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.order.OrderStatus;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * KitchenController — Dashboard dành cho KITCHEN (Nhân viên bếp).
 *
 * Luồng trách nhiệm của KITCHEN:
 *   CONFIRMED        → (start)  → PREPARING
 *   PREPARING        → (done)   → READY_FOR_PICKUP
 */
@Controller
@RequestMapping("/kitchen")
public class KitchenController {

    private final OrderService orderService;
    private final com.foodorderingsystem.service.FoodService foodService;
    private final UserRepository userRepository;

    public KitchenController(OrderService orderService,
                             com.foodorderingsystem.service.FoodService foodService,
                             UserRepository userRepository) {
        this.orderService = orderService;
        this.foodService = foodService;
        this.userRepository = userRepository;
    }

    /**
     * Màn hình bếp chính: hiển thị 2 cột
     *  - Cột trái : đơn CONFIRMED đang chờ bếp nhận
     *  - Cột phải : đơn PREPARING đang chế biến
     */
    @GetMapping
    public String kitchenScreen(Model model, java.security.Principal principal) {
        Restaurant workingRest = null;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                workingRest = user.getWorkingRestaurant();
            }
        }

        List<Order> confirmedOrders;
        List<Order> preparingOrders;

        if (workingRest != null) {
            confirmedOrders = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);
            preparingOrders = orderService.getOrdersByStatus(OrderStatus.PREPARING);
            final Long restId = workingRest.getRestaurantId();
            confirmedOrders = confirmedOrders.stream()
                    .filter(o -> o.getRestaurant() != null && restId.equals(o.getRestaurant().getRestaurantId()))
                    .collect(java.util.stream.Collectors.toList());
            preparingOrders = preparingOrders.stream()
                    .filter(o -> o.getRestaurant() != null && restId.equals(o.getRestaurant().getRestaurantId()))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            confirmedOrders = new java.util.ArrayList<>();
            preparingOrders = new java.util.ArrayList<>();
            model.addAttribute("warningMessage", "Tài khoản bếp của bạn chưa được liên kết với nhà hàng nào. Vui lòng liên hệ Admin!");
        }

        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("confirmedCount",  confirmedOrders.size());
        model.addAttribute("preparingCount",  preparingOrders.size());

        return "kitchen/index";
    }


    /** Xem chi tiết món cần nấu trong 1 đơn */
    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/kitchen";
        model.addAttribute("order", order);
        return "kitchen/order-detail";
    }

    /**
     * Bắt đầu chế biến: CONFIRMED → PREPARING
     */
    @PostMapping("/order/{id}/start")
    public String startPreparing(@PathVariable Long id, RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.CONFIRMED) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ!");
            return "redirect:/kitchen";
        }
        orderService.startPreparing(id);
        ra.addFlashAttribute("successMessage", "🍳 Đang chế biến đơn #" + id);
        return "redirect:/kitchen";
    }

    /**
     * Hoàn tất chế biến: PREPARING → READY_FOR_PICKUP
     * Sau bước này shipper có thể nhìn thấy đơn.
     */
    @PostMapping("/order/{id}/done")
    public String markDone(@PathVariable Long id, RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.PREPARING) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ!");
            return "redirect:/kitchen";
        }
        orderService.markReadyForPickup(id);
        ra.addFlashAttribute("successMessage", "✅ Đơn #" + id + " sẵn sàng giao — Shipper sẽ đến lấy!");
        return "redirect:/kitchen";
    }

    /** AJAX: Bắt đầu nấu */
    @PostMapping("/api/order/{id}/start")
    @ResponseBody
    public ResponseEntity<?> startPreparingAjax(@PathVariable Long id) {
        try {
            orderService.startPreparing(id);
            return ResponseEntity.ok("{\"success\":true,\"status\":\"PREPARING\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /** AJAX: Xong nấu */
    @PostMapping("/api/order/{id}/done")
    @ResponseBody
    public ResponseEntity<?> markDoneAjax(@PathVariable Long id) {
        try {
            orderService.markReadyForPickup(id);
            return ResponseEntity.ok("{\"success\":true,\"status\":\"READY_FOR_PICKUP\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Bếp hủy đơn đột xuất (Form POST)
     */
    @PostMapping("/order/{id}/cancel")
    public String cancelByKitchen(@PathVariable Long id,
                                  @RequestParam("reason") String reason,
                                  RedirectAttributes ra) {
        try {
            orderService.cancelOrderByKitchen(id, reason);
            ra.addFlashAttribute("successMessage", "❌ Đơn #" + id + " đã bị hủy bởi Bếp: " + reason);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/kitchen";
    }

    /**
     * AJAX: Bếp hủy đơn đột xuất
     */
    @PostMapping("/api/order/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelByKitchenAjax(@PathVariable Long id,
                                                  @RequestParam("reason") String reason) {
        try {
            orderService.cancelOrderByKitchen(id, reason);
            return ResponseEntity.ok("{\"success\":true,\"status\":\"CANCELLED\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Quản lý món ăn của bếp
     */
    @GetMapping("/foods")
    public String manageFoods(Model model, java.security.Principal principal) {
        Restaurant workingRest = null;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                workingRest = user.getWorkingRestaurant();
            }
        }

        List<Food> foods;
        if (workingRest != null) {
            foods = foodService.getFoodsByRestaurant(workingRest.getRestaurantId());
            model.addAttribute("restaurantName", workingRest.getName());
        } else {
            foods = new java.util.ArrayList<>();
            model.addAttribute("restaurantName", "Chưa liên kết");
            model.addAttribute("warningMessage", "Tài khoản bếp của bạn chưa được liên kết với nhà hàng nào. Vui lòng liên hệ Admin!");
        }

        model.addAttribute("foods", foods);
        return "kitchen/foods";
    }

    /**
     * AJAX toggle trạng thái món ăn
     */
    @PostMapping("/api/food/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleFoodStatusAjax(@PathVariable Long id) {
        try {
            foodService.toggleFoodStatus(id);
            return ResponseEntity.ok("{\"success\":true}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Xem lịch sử nấu của bếp với date filter chuyên nghiệp
     */
    @GetMapping("/history")
    public String kitchenHistory(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Model model,
            java.security.Principal principal
    ) {
        Restaurant workingRest = null;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                workingRest = user.getWorkingRestaurant();
            }
        }

        java.time.LocalDateTime start;
        java.time.LocalDateTime end;

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                start = java.time.LocalDate.parse(startDateStr, dtf).atStartOfDay();
            } else {
                start = java.time.LocalDate.now().atStartOfDay();
                startDateStr = java.time.LocalDate.now().toString();
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                end = java.time.LocalDate.parse(endDateStr, dtf).atTime(23, 59, 59);
            } else {
                end = java.time.LocalDate.now().atTime(23, 59, 59);
                endDateStr = java.time.LocalDate.now().toString();
            }
        } catch (Exception e) {
            start = java.time.LocalDate.now().atStartOfDay();
            end = java.time.LocalDate.now().atTime(23, 59, 59);
            startDateStr = java.time.LocalDate.now().toString();
            endDateStr = java.time.LocalDate.now().toString();
        }

        List<Order> historyOrders;
        if (workingRest != null) {
            historyOrders = orderService.getKitchenHistory(workingRest, start, end);
        } else {
            historyOrders = new java.util.ArrayList<>();
            model.addAttribute("warningMessage", "Tài khoản bếp của bạn chưa được liên kết với nhà hàng nào. Vui lòng liên hệ Admin!");
        }

        model.addAttribute("historyOrders", historyOrders);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        if (workingRest != null) {
            model.addAttribute("restaurantName", workingRest.getName());
        }

        return "kitchen/history";
    }
}

