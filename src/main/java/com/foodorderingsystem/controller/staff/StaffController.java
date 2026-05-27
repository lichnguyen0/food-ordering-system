package com.foodorderingsystem.controller.staff;

import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.order.OrderStatus;
import com.foodorderingsystem.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * StaffController — Dashboard dành cho STAFF (Nhân viên xử lý đơn hàng).
 *
 * Luồng trách nhiệm của STAFF:
 *   PENDING  → (confirm) → CONFIRMED
 *   PENDING  → (cancel)  → CANCELLED
 */
@Controller
@RequestMapping("/staff")
public class StaffController {

    private final OrderService orderService;

    public StaffController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Dashboard tổng quan của nhân viên */
    @GetMapping
    public String dashboard(Model model) {
        Pageable top10 = PageRequest.of(0, 10, Sort.by("orderId").descending());

        long pendingCount    = orderService.countByStatus(OrderStatus.PENDING);
        long confirmedCount  = orderService.countConfirmedToday();
        long cancelledToday  = orderService.countCancelledToday();

        model.addAttribute("pendingCount",   pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("cancelledToday", cancelledToday);
        model.addAttribute("recentPending",  orderService.getPendingOrders(top10).getContent());

        return "staff/index";
    }

    /** Danh sách đơn hàng PENDING chờ xác nhận */
    @GetMapping("/orders")
    public String orders(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderId").descending());

        Page<Order> pendingPage = orderService.getPendingOrders(pageable);

        model.addAttribute("pendingPage",  pendingPage);
        model.addAttribute("currentPage",  page);
        model.addAttribute("pageSize",     size);

        return "staff/orders";
    }

    /** Danh sách toàn bộ đơn hàng */
    @GetMapping("/all-orders")
    public String allOrders(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderId").descending());

        Page<Order> allOrdersPage = orderService.getAllOrdersPaginated(pageable);

        model.addAttribute("allOrdersPage",  allOrdersPage);
        model.addAttribute("currentPage",   page);
        model.addAttribute("pageSize",      size);

        return "staff/all-orders";
    }

    /** Xem chi tiết 1 đơn hàng */
    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/staff/orders";
        model.addAttribute("order", order);
        return "staff/order-detail";
    }

    /**
     * Xác nhận đơn hàng: PENDING → CONFIRMED
     * Sau khi confirm, đơn sẽ hiển thị trên màn hình bếp (KITCHEN).
     */
    @PostMapping("/order/{id}/confirm")
    public String confirmOrder(@PathVariable Long id,
                               Principal principal,
                               RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ hoặc đã được xử lý!");
            return "redirect:/staff/orders";
        }
        orderService.confirmOrder(id, principal != null ? principal.getName() : "staff");
        ra.addFlashAttribute("successMessage", "✅ Đã xác nhận đơn #" + id + " — Chuyển sang bếp!");
        return "redirect:/staff/orders";
    }

    /**
     * Hủy đơn hàng: PENDING → CANCELLED
     */
    @PostMapping("/order/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            ra.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng này!");
            return "redirect:/staff/orders";
        }
        orderService.cancelOrderByStaff(id, reason != null ? reason : "Nhân viên hủy đơn");
        ra.addFlashAttribute("successMessage", "🚫 Đã hủy đơn #" + id);
        return "redirect:/staff/orders";
    }

    /** AJAX endpoint cho confirm/cancel từ table */
    @PostMapping("/api/order/{id}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmOrderAjax(@PathVariable Long id, Principal principal) {
        try {
            orderService.confirmOrder(id, principal != null ? principal.getName() : "staff");
            return ResponseEntity.ok("{\"success\":true,\"message\":\"Đã xác nhận đơn hàng\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/api/order/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrderAjax(@PathVariable Long id,
                                             @RequestParam(required = false) String reason) {
        try {
            orderService.cancelOrderByStaff(id, reason != null ? reason : "Nhân viên hủy đơn");
            return ResponseEntity.ok("{\"success\":true,\"message\":\"Đã hủy đơn hàng\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
