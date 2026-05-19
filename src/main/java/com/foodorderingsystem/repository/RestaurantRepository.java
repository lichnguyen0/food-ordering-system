package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Tìm nhà hàng đang có ưu đãi ĐANG HOẠT ĐỘNG:
     * - hasPromo = true
     * - promoEndDate chưa hết hạn (hoặc chưa đặt ngày kết thúc)
     * - promoStartDate đã bắt đầu (hoặc chưa đặt ngày bắt đầu)
     */
    @Query("SELECT r FROM Restaurant r WHERE r.hasPromo = true " +
           "AND (r.promoEndDate IS NULL OR r.promoEndDate >= :today) " +
           "AND (r.promoStartDate IS NULL OR r.promoStartDate <= :today)")
    List<Restaurant> findActivePromos(@Param("today") LocalDate today);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.foods f WHERE f.category.categoryId = :categoryId")
    List<Restaurant> findByCategoryId(@Param("categoryId") Long categoryId);
}
