package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Order;
import com.foodorderingsystem.model.OrderStatus;
import com.foodorderingsystem.dto.MonthlyRevenue;
import com.foodorderingsystem.service.MonthlyStatService;
import org.springframework.http.ResponseEntity;

import com.foodorderingsystem.mapper.FoodMapper;
import com.foodorderingsystem.repository.CategoryRepository;
import com.foodorderingsystem.repository.FoodRepository;
import com.foodorderingsystem.repository.UserRepository;
import com.foodorderingsystem.service.OrderService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final MonthlyStatService monthlyStatService;
    private final CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;
    private final UserRepository userRepository;
    private final com.foodorderingsystem.service.InvoiceService invoiceService;

    public AdminController(OrderService orderService, MonthlyStatService monthlyStatService, FoodRepository foodRepository, FoodMapper foodMapper, com.foodorderingsystem.repository.CategoryRepository categoryRepository, UserRepository userRepository, com.foodorderingsystem.service.InvoiceService invoiceService) {
        this.orderService = orderService;
        this.monthlyStatService = monthlyStatService;
        this.foodRepository = foodRepository;
        this.foodMapper = foodMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
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
        // Người dùng hoạt động trong tháng hiện tại (MAU)
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        model.addAttribute("activeUsers", userRepository.countActiveUsersThisMonth(firstDayOfMonth));
        // Lấy top 5 đơn hàng doanh thu cao
        model.addAttribute("recentOrders", orderService.getTopRevenue(5));

        // --- Tính toán Doanh thu hàng tháng ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfCurrentMonth = currentMonthStart.plusMonths(1).minusSeconds(1);
        LocalDateTime start5MonthsAgo = YearMonth.from(currentMonthStart).minusMonths(5).atDay(1).atStartOfDay();

        List<MonthlyRevenue> stats = monthlyStatService.getRevenueByMonth(start5MonthsAgo, endOfCurrentMonth);
        model.addAttribute("monthlyRevenueStats", stats);

        double monthMaxRevenue = stats.stream()
                .mapToDouble(MonthlyRevenue::getRevenue)
                .max()
                .orElse(1.0);

        model.addAttribute("monthMaxRevenue", monthMaxRevenue);

        double lastMonthRevenue = 0.0;
        double currentMonthRevenue = 0.0;
        int statsSize = stats.size();

        if (statsSize >= 2) {
            MonthlyRevenue current = stats.get(statsSize - 1);
            MonthlyRevenue previous = stats.get(statsSize - 2);
            currentMonthRevenue = current.getRevenue();
            lastMonthRevenue = previous.getRevenue();
        } else if (statsSize == 1) {
            currentMonthRevenue = stats.get(0).getRevenue();
        }

        double growthPercent = (lastMonthRevenue > 0)
            ? ((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100.0
            : 0.0;

        model.addAttribute("lastMonthRevenue", lastMonthRevenue);
        model.addAttribute("currentMonthRevenue", currentMonthRevenue);
        model.addAttribute("growthPercent", growthPercent);

        return "admin/index";
    }

    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("invoices", invoiceService.getAllInvoices());
        return "admin/invoices/list";
    }

    @GetMapping("/invoices/{id}")
    public String invoiceDetail(@PathVariable Long id, Model model) {
        com.foodorderingsystem.model.Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return "redirect:/admin/invoices";
        }
        model.addAttribute("invoice", invoice);
        return "admin/invoices/detail";
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
    public String orders(Model model, 
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {

        // Create Pageable object with sorting by orderId descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderId").descending());

        // Get paginated orders for each status
        Page<Order> pendingOrdersPage = orderService.getPendingOrders(pageable);
        Page<Order> processingOrdersPage = orderService.getProcessingOrders(pageable);
        Page<Order> shippingOrdersPage = orderService.getShippingOrders(pageable);
        Page<Order> completedOrdersPage = orderService.getCompletedOrders(pageable);

        // Add page objects to model for displaying content
        model.addAttribute("pendingOrdersPage", pendingOrdersPage);
        model.addAttribute("processingOrdersPage", processingOrdersPage);
        model.addAttribute("shippingOrdersPage", shippingOrdersPage);
        model.addAttribute("completedOrdersPage", completedOrdersPage);

        // Add total counts for tab badges (using totalElements from pages)
        model.addAttribute("pendingCount", pendingOrdersPage.getTotalElements());
        model.addAttribute("processingCount", processingOrdersPage.getTotalElements());
        model.addAttribute("shippingCount", shippingOrdersPage.getTotalElements());
        model.addAttribute("completedCount", completedOrdersPage.getTotalElements());

        // Add pagination info to model
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

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
