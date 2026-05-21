package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Restaurant;
import com.foodorderingsystem.repository.RestaurantRepository;
import com.foodorderingsystem.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final FileService fileService;

    public AdminRestaurantController(RestaurantRepository restaurantRepository,
                                     FileService fileService) {
        this.restaurantRepository = restaurantRepository;
        this.fileService = fileService;
    }

    // List all restaurants
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);
        model.addAttribute("restaurants", restaurantPage.getContent());
        model.addAttribute("totalPages", restaurantPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", restaurantPage.getTotalElements());
        return "admin/restaurant-list";
    }

    // Hiển thị biểu mẫu thêm
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "admin/restaurant-form";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        model.addAttribute("restaurant", restaurant);
        return "admin/restaurant-form";
    }

    // Lưu (tạo hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute("restaurant") Restaurant restaurant,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes) throws IOException {

        // Xử lý tải lên hình ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileService.saveImage(imageFile);
            restaurant.setImage(imagePath);
        } else if (restaurant.getRestaurantId() != null) {
            // Giữ hình ảnh hiện có nếu chỉnh sửa và không có hình ảnh mới được tải lên
            Restaurant existing = restaurantRepository.findById(restaurant.getRestaurantId()).orElse(null);
            if (existing != null) {
                restaurant.setImage(existing.getImage());
            }
        }

        restaurantRepository.save(restaurant);
        redirectAttributes.addFlashAttribute("success", "Restaurant saved successfully!");
        return "redirect:/admin/restaurants";
    }

    // Delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        restaurantRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Restaurant deleted successfully!");
        return "redirect:/admin/restaurants";
    }
}
