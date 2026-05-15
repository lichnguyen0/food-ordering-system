package com.foodorderingsystem.controller;

import com.foodorderingsystem.repository.CategoryRepository;
import com.foodorderingsystem.repository.RestaurantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    public AuthController(CategoryRepository categoryRepository,
                          RestaurantRepository restaurantRepository) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        return "home";
    }
}

