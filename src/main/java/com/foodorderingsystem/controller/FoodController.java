package com.foodorderingsystem.controller;


import com.foodorderingsystem.service.FoodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }
    @GetMapping("/foods")
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId) {

        model.addAttribute("foods", foodService.search(keyword, categoryId));

        return "food-list";
    }

}
