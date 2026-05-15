package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.model.Restaurant;
import com.foodorderingsystem.repository.FoodRepository;
import com.foodorderingsystem.repository.RestaurantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final com.foodorderingsystem.repository.CollectionRepository collectionRepository;

    public RestaurantController(RestaurantRepository restaurantRepository,
                                FoodRepository foodRepository,
                                com.foodorderingsystem.repository.CollectionRepository collectionRepository) {
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
        this.collectionRepository = collectionRepository;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<Food> foods = foodRepository.findByRestaurant_RestaurantId(id);
        
        // Nhóm món ăn theo danh mục để làm giao diện Tab như ảnh cảm hứng
        java.util.Map<com.foodorderingsystem.model.Category, List<Food>> foodsByCategory = foods.stream()
                .filter(f -> f.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(Food::getCategory));

        model.addAttribute("restaurant", restaurant);
        
        // Lấy bộ sưu tập riêng của nhà hàng này theo loại (OFFER, RECOMMENDATION)
        model.addAttribute("offers", collectionRepository.findByTypeAndRestaurant_RestaurantId("OFFER", id)
                .map(com.foodorderingsystem.model.Collection::getFoods).orElse(java.util.Collections.emptyList()));
        model.addAttribute("recommendations", collectionRepository.findByTypeAndRestaurant_RestaurantId("RECOMMENDATION", id)
                .map(com.foodorderingsystem.model.Collection::getFoods).orElse(java.util.Collections.emptyList()));
        
        model.addAttribute("foodsByCategory", foodsByCategory);
        model.addAttribute("totalFoods", foods.size());
        return "restaurant-detail";
    }
}
