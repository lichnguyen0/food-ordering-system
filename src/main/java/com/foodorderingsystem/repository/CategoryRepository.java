package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
