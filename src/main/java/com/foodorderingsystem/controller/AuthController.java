package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.User;
import com.foodorderingsystem.model.UserRole;
import com.foodorderingsystem.repository.CategoryRepository;
import com.foodorderingsystem.repository.RestaurantRepository;
import com.foodorderingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(CategoryRepository categoryRepository,
                          RestaurantRepository restaurantRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByDisplayOrderAsc());
        // Chỉ lấy nhà hàng có ưu đãi đang hoạt động cho trang Home
        model.addAttribute("restaurants", restaurantRepository.findActivePromos(java.time.LocalDate.now()));
        return "user/home";
    }
}

