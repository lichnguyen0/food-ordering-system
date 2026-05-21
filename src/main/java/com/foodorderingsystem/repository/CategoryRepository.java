package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();

    Page<Category> findByCategoryNameContaining(String name, Pageable pageable);

    Page<Category> findByActiveTrue(Pageable pageable);
}
