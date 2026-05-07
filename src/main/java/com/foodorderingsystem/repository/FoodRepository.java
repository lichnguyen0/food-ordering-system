package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByFoodNameContaining(String name);

    List<Food> findByCategory_CategoryId(Long categoryId);
}
