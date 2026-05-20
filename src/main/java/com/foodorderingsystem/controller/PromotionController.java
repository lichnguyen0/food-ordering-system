package com.foodorderingsystem.controller;

import com.foodorderingsystem.repository.RestaurantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    private final RestaurantRepository restaurantRepository;

    public PromotionController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public String promotions(Model model) {
        // Chỉ lấy nhà hàng có ưu đãi đang HOẠT ĐỘNG (chưa hết hạn)
        model.addAttribute("promoRestaurants", restaurantRepository.findActivePromos(LocalDate.now()));
        return "user/promotions";
    }
}
