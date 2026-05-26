package com.foodorderingsystem.controller.user;

import com.foodorderingsystem.model.category.Category;
import com.foodorderingsystem.model.collection.Collection;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.repository.category.CategoryRepository;
import com.foodorderingsystem.repository.collection.CollectionRepository;
import com.foodorderingsystem.repository.coupon.CouponRepository;
import com.foodorderingsystem.repository.food.FoodRepository;
import com.foodorderingsystem.repository.restaurant.RestaurantRepository;
import com.foodorderingsystem.service.DistanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final DistanceService distanceService;

    public RestaurantController(RestaurantRepository restaurantRepository,
                                 FoodRepository foodRepository,
                                 CollectionRepository collectionRepository,
                                 CategoryRepository categoryRepository,
                                 CouponRepository couponRepository,
                                 DistanceService distanceService) {
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
        this.collectionRepository = collectionRepository;
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.distanceService = distanceService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long categoryId, 
                       @RequestParam(required = false) Double userLat,
                       @RequestParam(required = false) Double userLng,
                       Model model) {
        List<Restaurant> restaurants;
        if (categoryId != null) {
            restaurants = restaurantRepository.findByCategoryId(categoryId);
            model.addAttribute("selectedCategory", categoryRepository.findById(categoryId).orElse(null));
        } else {
            restaurants = restaurantRepository.findAll();
        }

        boolean isFilteredByLocation = false;

        // Professional server-side filtering: only show restaurants within 7km if user location is provided
        if (userLat != null && userLng != null) {
            final double MAX_DISTANCE_KM = 7.0;
            restaurants = restaurants.stream()
                    .filter(r -> {
                        if (!r.hasValidCoordinates()) return false;
                        double dist = distanceService.calculateDistance(userLat, userLng, r.getLatitude(), r.getLongitude());
                        return dist <= MAX_DISTANCE_KM;
                    })
                    .collect(Collectors.toList());
            isFilteredByLocation = true;
        }
        
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByDisplayOrderAsc());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("userLat", userLat);
        model.addAttribute("userLng", userLng);
        model.addAttribute("isFilteredByLocation", isFilteredByLocation);
        return "user/restaurant-list-user";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, 
                         @RequestParam(required = false) Double userLat,
                         @RequestParam(required = false) Double userLng,
                         Model model) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<Food> foods = foodRepository.findByRestaurant_RestaurantId(id);

        // Nhóm món ăn theo danh mục để làm giao diện Tab
        Map<Category, List<Food>> foodsByCategory = foods.stream()
                .filter(f -> f.getCategory() != null)
                .collect(Collectors.groupingBy(Food::getCategory));

        model.addAttribute("restaurant", restaurant);

        // Tính toán khoảng cách nếu vị trí người dùng cung cấp
        if (userLat != null && userLng != null && restaurant.hasValidCoordinates()) {
            double distance = distanceService.calculateDistance(
                userLat, userLng,
                restaurant.getLatitude(), restaurant.getLongitude()
            );
            model.addAttribute("calculatedDistance", distance);
            model.addAttribute("estimatedDeliveryTime", distanceService.calculateDeliveryTime(distance));
        } else {
            model.addAttribute("estimatedDeliveryTime", restaurant.getDeliveryTime());
        }

        // Lấy bộ sưu tập riêng của nhà hàng này theo loại (OFFER, RECOMMENDATION)
        model.addAttribute("offers",
                collectionRepository
                        .findByTypeAndRestaurant_RestaurantId("OFFER", id)
                        .map(Collection::getFoods)
                        .orElse(java.util.Collections.emptyList())
        );

        model.addAttribute("recommendations",
                collectionRepository
                        .findByTypeAndRestaurant_RestaurantId("RECOMMENDATION", id)
                        .map(Collection::getFoods)
                        .orElse(java.util.Collections.emptyList())
        );

        model.addAttribute("foodsByCategory", foodsByCategory);
        model.addAttribute("totalFoods", foods.size());
        model.addAttribute("activeCoupons", couponRepository.findAllByActiveTrue());
        model.addAttribute("userLat", userLat);
        model.addAttribute("userLng", userLng);

        return "user/restaurant-detail";
    }
}
