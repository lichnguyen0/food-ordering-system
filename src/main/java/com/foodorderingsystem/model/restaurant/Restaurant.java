package com.foodorderingsystem.model.restaurant;

import com.foodorderingsystem.model.category.Category;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.order.Order;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    private Double latitude;
    private Double longitude;

    private Double rating;

    // e.g., "07:00-14:00 17:30-21:00"
    private String operatingHours;

    private String image;

    @Column(name = "has_promo", nullable = false)
    private boolean hasPromo = false;

    // % giảm giá (ví dụ: 20.0 = giảm 20%)
    @Column(name = "promo_discount")
    private Double promoDiscount;

    // Ngày bắt đầu và kết thúc ưu đãi
    @Column(name = "promo_start_date")
    private LocalDate promoStartDate;

    @Column(name = "promo_end_date")
    private LocalDate promoEndDate;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> foods;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Order> orders;

    @Transient
    public String getCategoriesString() {
        if (foods == null || foods.isEmpty()) {
            return "Đồ ăn";
        }
        return foods.stream()
            .map(Food::getCategory)
            .filter(java.util.Objects::nonNull)
            .map(Category::getCategoryName)
            .distinct()
            .collect(java.util.stream.Collectors.joining(", "));
    }

    @Transient
    public int getDeliveryTime() {
        if (restaurantId == null) return 25;
        return 15 + (int)(restaurantId % 6) * 5;
    }

    @Transient
    public double getDistance() {
        if (restaurantId == null) return 1.5;
        return 0.5 + (double)(restaurantId % 9) * 0.5;
    }

    @Transient
    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null 
            && latitude != 0.0 && longitude != 0.0;
    }

    @Transient
    public boolean isOpen() {
        if (operatingHours == null || operatingHours.isEmpty()) {
            return false;
        }
        
        java.time.LocalTime now = java.time.LocalTime.now();
        String[] periods = operatingHours.split("\\s+");
        
        for (String period : periods) {
            String[] times = period.split("-");
            if (times.length == 2) {
                try {
                    java.time.LocalTime openTime = java.time.LocalTime.parse(times[0]);
                    java.time.LocalTime closeTime = java.time.LocalTime.parse(times[1]);
                    
                    if ((now.isAfter(openTime) || now.equals(openTime)) && 
                        (now.isBefore(closeTime) || now.equals(closeTime))) {
                        return true;
                    }
                } catch (Exception e) {
                    // Invalid time format, skip
                }
            }
        }
        return false;
    }

    @Transient
    public String getOpenStatus() {
        return isOpen() ? "Đang mở cửa" : "Đóng cửa";
    }

    @Transient
    public String getStatusClass() {
        return isOpen() ? "open" : "closed";
    }
}
