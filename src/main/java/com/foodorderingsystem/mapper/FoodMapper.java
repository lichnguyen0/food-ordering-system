package com.foodorderingsystem.mapper;

import com.foodorderingsystem.dto.FoodDTO;
import com.foodorderingsystem.model.Category;
import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.repository.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
public class FoodMapper {

    private final CategoryRepository categoryRepository;
    private final com.foodorderingsystem.repository.RestaurantRepository restaurantRepository;

    public FoodMapper(CategoryRepository categoryRepository, com.foodorderingsystem.repository.RestaurantRepository restaurantRepository) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public FoodDTO toDTO(Food food) {
        FoodDTO dto = new FoodDTO();
        dto.setFoodId(food.getFoodId());
        dto.setFoodName(food.getFoodName());
        dto.setPrice(food.getPrice());
        dto.setDiscountPrice(food.getDiscountPrice());
        dto.setDescription(food.getDescription());
        dto.setImage(food.getImage());
        dto.setStatus(food.getStatus());
        if (food.getCategory() != null) {
            dto.setCategoryId(food.getCategory().getCategoryId());
            dto.setCategoryName(food.getCategory().getCategoryName());
        }

        if (food.getRestaurant() != null) {
            dto.setRestaurantId(food.getRestaurant().getRestaurantId());
            dto.setRestaurantName(food.getRestaurant().getName());
        }
        
        if (food.getImages() != null) {
            dto.setAdditionalImages(food.getImages().stream()
                .map(com.foodorderingsystem.model.FoodImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList()));
        }
        
        return dto;
    }

    public Food toEntity(FoodDTO dto) {
        Food food = new Food();
        food.setFoodId(dto.getFoodId());
        food.setFoodName(dto.getFoodName());
        food.setPrice(dto.getPrice());
        food.setDiscountPrice(dto.getDiscountPrice());
        food.setDescription(dto.getDescription());
        food.setImage(dto.getImage());
        food.setStatus(dto.getStatus());
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            food.setCategory(category);
        }

        if (dto.getRestaurantId() != null) {
            com.foodorderingsystem.model.Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId()).orElse(null);
            food.setRestaurant(restaurant);
        }

        if (dto.getAdditionalImages() != null) {
            Food finalFood = food;
            food.setImages(dto.getAdditionalImages().stream()
                .map(url -> new com.foodorderingsystem.model.FoodImage(url, finalFood))
                .collect(java.util.stream.Collectors.toList()));
        }
        
        return food;
    }
}
