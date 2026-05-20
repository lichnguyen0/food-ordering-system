package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
//kho lưu trữ danh mục
public interface CategoryRepository extends JpaRepository<Category, Long> {
    //tìm all category = true sắp xếp theo thứ tự tăng dần
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
}
