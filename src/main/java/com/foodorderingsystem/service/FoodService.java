package com.foodorderingsystem.service;

import com.foodorderingsystem.model.Food;

import java.util.List;

public interface FoodService {
    List<Food> getAllFoods();
    List<Food> search(String keyword, Long categoryId);

}
