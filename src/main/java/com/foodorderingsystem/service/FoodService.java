package com.foodorderingsystem.service;

import com.foodorderingsystem.model.food.Food;

import java.util.List;

public interface FoodService {
    List<Food> getAllFoods();
    List<Food> search(String keyword, Long categoryId);
    void toggleFoodStatus(Long foodId);
    List<Food> getFoodsByRestaurant(Long restaurantId);
}
