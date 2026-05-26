package com.foodorderingsystem.controller.user;

import com.foodorderingsystem.dto.CheckoutRequest;
import com.foodorderingsystem.model.cart.Cart;
import com.foodorderingsystem.model.cart.CartItem;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.order.OrderItem;
import com.foodorderingsystem.model.order.OrderStatus;
import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.coupon.CouponRepository;
import com.foodorderingsystem.repository.restaurant.RestaurantRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.service.CartService;
import com.foodorderingsystem.service.DistanceService;
import com.foodorderingsystem.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes("cart")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;
    private final DistanceService distanceService;
    private final RestaurantRepository restaurantRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository,
                           CouponRepository couponRepository,
                           CartService cartService,
                           DistanceService distanceService,
                           RestaurantRepository restaurantRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.cartService = cartService;
        this.distanceService = distanceService;
        this.restaurantRepository = restaurantRepository;
    }


    @GetMapping("/checkout")
    public String checkoutPage(Model model, 
                               @ModelAttribute("cart") Cart cart,
                               @RequestParam(required = false) Double userLat,
                               @RequestParam(required = false) Double userLng) {

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/"; // Chuyển hướng về trang chủ nếu giỏ hàng trống
        }

        double deliveryFee = 16000.0;
        double distance = 1.5;
        int deliveryTime = 25;

        CartItem firstItem = cart.getItems().values().iterator().next();

        Restaurant restaurant = null;

        if (firstItem.getFood() != null
                && firstItem.getFood().getRestaurant() != null) {

            Long restaurantId = firstItem.getFood().getRestaurant().getRestaurantId();
            
            // Tải nhà hàng một cách an toàn để tránh LazyInitializationException
            restaurant = restaurantRepository.findById(restaurantId).orElse(null);
        }

        if (restaurant != null) {
            // Tính toán khoảng cách bằng cách sử dụng tọa độ thực tế nếu có
            if (userLat != null && userLng != null 
                    && restaurant.hasValidCoordinates()) {
                distance = distanceService.calculateDistance(
                    userLat, userLng,
                    restaurant.getLatitude(), restaurant.getLongitude()
                );
            } else if (restaurant.getRestaurantId() != null) {
                // Dự phòng về khoảng cách giả ngẫu nhiên dựa trên ID
                distance = 0.5 + (double) (restaurant.getRestaurantId() % 9) * 0.5;
            }

            deliveryTime = distanceService.calculateDeliveryTime(distance);
            double rawFee = distanceService.calculateTieredDeliveryFee(distance);
            deliveryFee = (rawFee < 0) ? 0 : rawFee;
        }

        model.addAttribute("cart", cart);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("deliveryDistance", distance);
        model.addAttribute("deliveryTime", deliveryTime);

        boolean deliveryAvailable = distanceService.isDeliveryAvailable(distance);
        model.addAttribute("deliveryAvailable", deliveryAvailable);
        model.addAttribute("activeCoupons", couponRepository.findAllByActiveTrue());
        model.addAttribute("userLat", userLat);
        model.addAttribute("userLng", userLng);

        // Expose restaurant coordinates for frontend dynamic calculation
        if (restaurant != null) {
            model.addAttribute("restaurantLat", restaurant.getLatitude());
            model.addAttribute("restaurantLng", restaurant.getLongitude());
        }

        return "order/checkout";
    }


    @PostMapping("/order/checkout")
    public String checkout(@ModelAttribute("cart") Cart cart,
                           @ModelAttribute CheckoutRequest request,
                           Principal principal,
                           HttpSession session,
                           SessionStatus status) {

        Long userId = 1L; // Fallback

        if (principal != null) {
            User user = userRepository
                    .findByUsername(principal.getName())
                    .orElse(null);

            if (user != null) {
                userId = user.getUserId();
            }
        }

        Order order = orderService.createOrderFromCart(userId, cart, request);

        // Xóa giỏ hàng persistent trong DB sau khi đặt hàng thành công
        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        }
        cartService.clearCart(session, currentUser);

        // Đánh dấu phiên là hoàn tất để xóa giỏ hàng session attribute
        status.setComplete();

        if (order != null) {
            return "redirect:/order/tracking/" + order.getOrderId();
        }

        return "redirect:/";
    }

    @GetMapping("/orders")
    public String list(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();

        List<Order> userOrders = orderService.getOrdersByUsername(username);

        List<Order> activeOrders = userOrders.stream()
                .filter(o -> {
                    String status = o.getStatus().name();
                    return status.equals("PENDING")
                            || status.equals("CONFIRMED")
                            || status.equals("PREPARING")
                            || status.equals("READY_FOR_PICKUP")
                            || status.equals("DELIVERING");
                })
                .collect(Collectors.toList());

        List<Order> historyOrders = userOrders.stream()
                .filter(o -> {
                    String status = o.getStatus().name();
                    return status.equals("DELIVERED")
                            || status.equals("COMPLETED")
                            || status.equals("CANCELLED");
                })
                .collect(Collectors.toList());

        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("historyOrders", historyOrders);
        model.addAttribute("allOrders", userOrders);

        return "order/order";
    }

    @GetMapping("/order/tracking/{id}")
    public String trackingPage(@PathVariable("id") Long id, Model model) {

        Order order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);

        return "order/order-tracking";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderById(id);

        if (order != null
                && order.getStatus() == OrderStatus.PENDING
                && order.getUser().getUsername().equals(principal.getName())) {

            orderService.updateStatus(id, OrderStatus.CANCELLED);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Hủy đơn hàng thành công!"
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể hủy đơn hàng này!"
            );
        }

        return "redirect:/orders";
    }


    @PostMapping("/order/reorder/{id}")
    public String reorder(@PathVariable("id") Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderById(id);

        if (order != null) {

            Cart cart = (Cart) session.getAttribute("cart");

            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }

            for (OrderItem item : order.getOrderItems()) {

                Food food = item.getFood();
                int quantity = item.getQuantity();

                for (int i = 0; i < quantity; i++) {
                    cart.add(food);
                }
            }

            session.setAttribute("cart", cart);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã thêm các món vào giỏ hàng!"
            );
            return "redirect:/checkout";
        }

        return "redirect:/orders";
    }

}
