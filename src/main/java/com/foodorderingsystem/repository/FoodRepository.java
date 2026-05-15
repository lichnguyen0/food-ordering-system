package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByFoodNameContaining(String name);

    List<Food> findByCategory_CategoryId(Long categoryId);
    org.springframework.data.domain.Page<Food> findByCategory_CategoryId(Long categoryId, org.springframework.data.domain.Pageable pageable);

    List<Food> findByRestaurant_RestaurantId(Long restaurantId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Food f " +
           "LEFT JOIN f.category c " +
           "LEFT JOIN f.restaurant r " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Food> search(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
