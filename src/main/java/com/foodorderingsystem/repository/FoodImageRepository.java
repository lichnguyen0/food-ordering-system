package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.FoodImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//kho lưu trữ hình ảnh thực phẩm
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
}
