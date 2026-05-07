package com.foodorderingsystem.service;

import com.foodorderingsystem.model.Food;

import java.util.List;

public interface FoodService {
    List<Food> getAll();
    List<Food> search(String keyword, Long categoryId);
}
