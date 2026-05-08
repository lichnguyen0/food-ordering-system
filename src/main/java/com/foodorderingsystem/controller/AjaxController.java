package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AjaxController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/food-by-category")
    public List<Food> getFoodsByCategory(@RequestParam Long categoryId) {
        return foodService.search(null, categoryId);
    }
}
