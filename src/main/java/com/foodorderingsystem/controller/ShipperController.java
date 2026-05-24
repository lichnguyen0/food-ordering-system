package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderStatus;
import com.foodorderingsystem.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.foodorderingsystem.model.User;
import com.foodorderingsystem.repository.UserRepository;

import java.security.Principal;
import java.util.List;

/**
 * ShipperController — Dashboard dành cho SHIPPER (Nhân viên giao hàng).
 *
 * Luồng trách nhiệm của SHIPPER:
 *   READY_FOR_PICKUP → (pickup)    → DELIVERING
 *   DELIVERING       → (delivered) → DELIVERED
 */
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public ShipperController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * Dashboard chính của shipper:
     *  - Đơn đang giao (DELIVERING) của bản thân
     *  - Số đơn READY_FOR_PICKUP đang chờ
     */
    @GetMapping
    public String dashboard(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "";

        List<Order> myDeliveries  = orderService.getMyDeliveries(username);
        List<Order> availableOrders = orderService.getOrdersByStatus(OrderStatus.READY_FOR_PICKUP);

        boolean isAvailable = true;
        if (!username.isEmpty()) {
            User shipper = userRepository.findByUsername(username).orElse(null);
            if (shipper != null && shipper.getAvailable() != null) {
                isAvailable = shipper.getAvailable();
            }
        }

        model.addAttribute("myDeliveries",    myDeliveries);
        model.addAttribute("availableCount",  availableOrders.size());
        model.addAttribute("deliveringCount", myDeliveries.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERING).count());
        model.addAttribute("isAvailable", isAvailable);

        return "shipper/index";
    }

    /**
     * Danh sách đơn READY_FOR_PICKUP — shipper có thể nhận
     */
    @GetMapping("/available")
    public String availableOrders(Model model) {
        List<Order> orders = orderService.getOrdersByStatus(OrderStatus.READY_FOR_PICKUP);
        model.addAttribute("availableOrders", orders);
        return "shipper/available";
    }

    /**
     * Lịch sử giao hàng của shipper này (DELIVERED)
     */
    @GetMapping("/history")
    public String history(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "";
        List<Order> history = orderService.getShipperHistory(username);
        model.addAttribute("historyOrders", history);
        return "shipper/history";
    }

    /**
     * Nhận đơn để giao: READY_FOR_PICKUP → DELIVERING
     * Gắn shipper_id vào đơn hàng.
     */
    @PostMapping("/order/{id}/pickup")
    public String pickupOrder(@PathVariable Long id,
                              Principal principal,
                              RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng không còn khả dụng!");
            return "redirect:/shipper/available";
        }
        String username = principal != null ? principal.getName() : "";
        orderService.pickupOrder(id, username);
        ra.addFlashAttribute("successMessage", "🛵 Đã nhận đơn #" + id + " — Bắt đầu giao hàng!");
        return "redirect:/shipper";
    }

    /**
     * Xác nhận giao thành công: DELIVERING → DELIVERED
     */
    @PostMapping("/order/{id}/delivered")
    public String confirmDelivered(@PathVariable Long id,
                                   Principal principal,
                                   RedirectAttributes ra) {
        Order order = orderService.getOrderById(id);
        if (order == null || order.getStatus() != OrderStatus.DELIVERING) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ!");
            return "redirect:/shipper";
        }
        orderService.completeDelivery(id);
        ra.addFlashAttribute("successMessage", "✅ Đơn #" + id + " đã giao thành công!");
        return "redirect:/shipper";
    }

    /** AJAX: Nhận đơn */
    @PostMapping("/api/order/{id}/pickup")
    @ResponseBody
    public ResponseEntity<?> pickupAjax(@PathVariable Long id, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : "";
            orderService.pickupOrder(id, username);
            return ResponseEntity.ok("{\"success\":true,\"status\":\"DELIVERING\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /** AJAX: Xác nhận giao thành công */
    @PostMapping("/api/order/{id}/delivered")
    @ResponseBody
    public ResponseEntity<?> deliveredAjax(@PathVariable Long id) {
        try {
            orderService.completeDelivery(id);
            return ResponseEntity.ok("{\"success\":true,\"status\":\"DELIVERED\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Báo giao thất bại / Hủy đơn (Form POST)
     */
    @PostMapping("/order/{id}/fail")
    public String failDelivery(@PathVariable Long id,
                               @RequestParam("reason") String reason,
                               RedirectAttributes ra) {
        try {
            orderService.cancelOrderByShipper(id, reason);
            ra.addFlashAttribute("successMessage", "Đã báo cáo giao hàng thất bại cho đơn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/shipper";
    }

    /** AJAX: Cập nhật trạng thái sẵn sàng (Available) */
    @PostMapping("/api/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(Principal principal) {
        try {
            String username = principal != null ? principal.getName() : "";
            if (username.isEmpty()) throw new IllegalStateException("Không tìm thấy user");
            
            User shipper = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User không tồn tại"));
            boolean newStatus = shipper.getAvailable() == null ? true : !shipper.getAvailable();
            shipper.setAvailable(newStatus);
            userRepository.save(shipper);
            
            return ResponseEntity.ok("{\"success\":true,\"available\":" + newStatus + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
