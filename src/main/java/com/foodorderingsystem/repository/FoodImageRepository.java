package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.FoodImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
}
