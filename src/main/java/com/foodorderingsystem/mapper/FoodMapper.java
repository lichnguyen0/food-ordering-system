package com.foodorderingsystem.mapper;
//Mapper là lớp chuyên dùng để chuyển đổi object. Cụ thể Chuyển: DTO ↔ Entity
import com.foodorderingsystem.dto.FoodDTO;
import com.foodorderingsystem.model.category.Category;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.food.FoodImage;
import com.foodorderingsystem.model.option.OptionGroup;
import com.foodorderingsystem.model.option.OptionItem;
import com.foodorderingsystem.model.restaurant.Restaurant;
import com.foodorderingsystem.repository.category.CategoryRepository;
import com.foodorderingsystem.repository.restaurant.RestaurantRepository;
import org.springframework.stereotype.Component;

@Component
public class FoodMapper {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    public FoodMapper(CategoryRepository categoryRepository, RestaurantRepository restaurantRepository) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public FoodDTO toDTO(Food food) {  // nhiệm vụ chuyển object Food Entity thành FoodDTO
        FoodDTO dto = new FoodDTO();
        dto.setFoodId(food.getFoodId()); //Mapping field. ý nghĩa lấy dữ liệu từ food copy sang dto
        dto.setFoodName(food.getFoodName());
        dto.setPrice(food.getPrice());
        dto.setDiscountPrice(food.getDiscountPrice()); // cho frontend biết giá giảm
        dto.setDescription(food.getDescription());
        dto.setImage(food.getImage());
        dto.setStatus(food.getStatus());
        if (food.getCategory() != null) { //Nếu category của food khác null  // thức ăn có danh mục
            dto.setCategoryId(food.getCategory().getCategoryId()); //category từ food// Lấy categoryId từ category vừa lấy được.Gán giá trị vừa lấy vào DTO.
            dto.setCategoryName(food.getCategory().getCategoryName());
        }

        if (food.getRestaurant() != null) {
            dto.setRestaurantId(food.getRestaurant().getRestaurantId());
            dto.setRestaurantName(food.getRestaurant().getName());
        }
        
        if (food.getImages() != null) {
            dto.setAdditionalImages(food.getImages().stream()
                .map(FoodImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList()));
        }
        if (food.getOptionGroups() != null) {
            dto.setOptionGroups(food.getOptionGroups().stream().map(g -> {
                FoodDTO.OptionGroupDTO gDTO = new FoodDTO.OptionGroupDTO();
                gDTO.setGroupId(g.getGroupId());
                gDTO.setGroupName(g.getGroupName());
                gDTO.setRequired(g.isRequired());
                gDTO.setMultiple(g.isMultiple());
                
                if (g.getOptionItems() != null) {
                    gDTO.setOptionItems(g.getOptionItems().stream().map(i -> {
                        FoodDTO.OptionItemDTO iDTO = new FoodDTO.OptionItemDTO();
                        iDTO.setItemId(i.getItemId());
                        iDTO.setItemName(i.getItemName());
                        iDTO.setExtraPrice(i.getExtraPrice());
                        return iDTO;
                    }).collect(java.util.stream.Collectors.toList()));
                }
                return gDTO;
            }).collect(java.util.stream.Collectors.toList()));
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
            Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId()).orElse(null);
            food.setRestaurant(restaurant);
        }

        if (dto.getAdditionalImages() != null) {
            Food finalFood = food;
            food.setImages(dto.getAdditionalImages().stream()
                .map(url -> new FoodImage(url, finalFood))
                .collect(java.util.stream.Collectors.toList()));
        }
        
        if (dto.getOptionGroups() != null) {
            Food finalFood = food;
            food.setOptionGroups(dto.getOptionGroups().stream().map(gDTO -> {
                OptionGroup g = new OptionGroup();
                g.setGroupId(gDTO.getGroupId());
                g.setGroupName(gDTO.getGroupName());
                g.setRequired(gDTO.isRequired());
                g.setMultiple(gDTO.isMultiple());
                g.setFood(finalFood);
                
                if (gDTO.getOptionItems() != null) {
                    g.setOptionItems(gDTO.getOptionItems().stream().map(iDTO -> {
                        OptionItem i = new OptionItem();
                        i.setItemId(iDTO.getItemId());
                        i.setItemName(iDTO.getItemName());
                        i.setExtraPrice(iDTO.getExtraPrice());
                        i.setOptionGroup(g);
                        return i;
                    }).collect(java.util.stream.Collectors.toList()));
                }
                return g;
            }).collect(java.util.stream.Collectors.toList()));
        }

        return food;
    }
}
