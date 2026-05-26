package com.foodorderingsystem.controller.user;

import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.model.user.UserRole;
import com.foodorderingsystem.repository.category.CategoryRepository;
import com.foodorderingsystem.repository.restaurant.RestaurantRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.foodorderingsystem.service.FoodService foodService;
    private final com.foodorderingsystem.service.DistanceService distanceService;

    public AuthController(CategoryRepository categoryRepository,
                          RestaurantRepository restaurantRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          com.foodorderingsystem.service.FoodService foodService,
                          com.foodorderingsystem.service.DistanceService distanceService) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.foodService = foodService;
        this.distanceService = distanceService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        
        model.addAttribute("fullName", fullName);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ các thông tin bắt buộc.");
            return "auth/register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không trùng khớp.");
            return "auth/register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại trong hệ thống.");
            return "auth/register";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Địa chỉ Email đã được đăng ký.");
            return "auth/register";
        }
        
        // Lưu user mới
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER); // Mặc định đăng ký mới là USER
        
        userRepository.save(user);
        
        return "redirect:/login?registerSuccess";
    }

    @GetMapping("/")
    public String home(Model model,
                        @RequestParam(required = false) Double userLat,
                        @RequestParam(required = false) Double userLng) {
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByDisplayOrderAsc());

        List<Restaurant> promoRestaurants = 
            restaurantRepository.findActivePromos(java.time.LocalDate.now());

        boolean isFilteredByLocation = false;

        // Lọc chuyên nghiệp: chỉ hiển thị các nhà hàng trong bán kính giao hàng 7km khi có vị trí
        if (userLat != null && userLng != null) {
            final double MAX_DISTANCE_KM = 7.0;
            promoRestaurants = promoRestaurants.stream()
                    .filter(r -> {
                        if (!r.hasValidCoordinates()) return false;
                        double dist = distanceService.calculateDistance(userLat, userLng, r.getLatitude(), r.getLongitude());
                        return dist <= MAX_DISTANCE_KM;
                    })
                    .collect(Collectors.toList());
            isFilteredByLocation = true;
        }

        model.addAttribute("restaurants", promoRestaurants);
        model.addAttribute("foods", foodService.getAllFoods());
        model.addAttribute("userLat", userLat);
        model.addAttribute("userLng", userLng);
        model.addAttribute("isFilteredByLocation", isFilteredByLocation);
        return "user/home";
    }
}

