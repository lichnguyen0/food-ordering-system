package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.food.FoodStatus;
import com.foodorderingsystem.repository.food.FoodRepository;
import com.foodorderingsystem.service.FoodService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;

    public FoodServiceImpl(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Override
    public List<Food> search(String keyword, Long categoryId) {

        if (keyword != null && !keyword.isEmpty()) {
            return foodRepository.findByFoodNameContaining(keyword);
        }
        if (categoryId != null) {
            return foodRepository.findByCategory_CategoryId(categoryId);
        }
        return foodRepository.findAll();
    }

    @Override
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    @Override
    public void toggleFoodStatus(Long foodId) {
        Food food = foodRepository.findById(foodId).orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại"));
        if (food.getStatus() == FoodStatus.AVAILABLE) {
            food.setStatus(FoodStatus.SOLD_OUT);
        } else {
            food.setStatus(FoodStatus.AVAILABLE);
        }
        foodRepository.save(food);
    }

    @Override
    public List<Food> getFoodsByRestaurant(Long restaurantId) {
        return foodRepository.findByRestaurant_RestaurantId(restaurantId);
    }

    public List<Food> getByCategoryId(Long categoryId) {
        return foodRepository.findByCategory_CategoryId(categoryId);
    }
}