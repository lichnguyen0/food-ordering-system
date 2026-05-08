package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.service.FoodService;
import com.foodorderingsystem.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;
    private final CategoryRepository categoryRepository;

    public FoodController(FoodService foodService, CategoryRepository categoryRepository) {
        this.foodService = foodService;
        this.categoryRepository = categoryRepository;
    }
    @GetMapping
    public String menu(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId) {

        List<Food> foods = foodService.search(keyword, categoryId);

        model.addAttribute("foods", foods);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);

        return "menu";
    }

    @GetMapping("/filter")
    public String filterByCategory(@RequestParam(required = false) Long categoryId, Model model) {
        List<Food> foods = foodService.search(null, categoryId);
        model.addAttribute("foods", foods);
        return "fragments/food-table :: foodTable";
    }

}
