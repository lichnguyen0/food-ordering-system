package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//kho lưu trữ bộ sưu tập
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    /*Optional<Collection> findByName(String name);*/
    
    // Tìm bộ sưu tập theo loại và nhà hàng
    Optional<Collection> findByTypeAndRestaurant_RestaurantId(String type, Long restaurantId);
    
    /*// Tìm tất cả bộ sưu tập của một nhà hàng
    List<Collection> findByRestaurant_RestaurantId(Long restaurantId);*/
}
