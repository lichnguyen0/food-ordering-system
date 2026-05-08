package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.repository.FoodRepository;
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

    public List<Food> getByCategoryId(Long categoryId) {
        return foodRepository.findByCategory_CategoryId(categoryId);
    }
}