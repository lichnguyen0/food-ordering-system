package com.foodorderingsystem.dto;

import com.foodorderingsystem.model.FoodStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FoodDTO {
    private Long foodId;

    @NotBlank(message = "Tên món ăn không được để trống")
    private String foodName;

    @Positive(message = "Giá phải lớn hơn 0")
    private double price;

    private Double discountPrice; // Thêm trường này để nhận dữ liệu từ form

    private String description;
    private String image;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "Trạng thái không được để trống")
    private FoodStatus status;

    @NotNull(message = "Vui lòng chọn nhà hàng")
    private Long restaurantId;

    private String restaurantName;

    private java.util.List<String> additionalImages = new java.util.ArrayList<>();

    private java.util.List<OptionGroupDTO> optionGroups = new java.util.ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class OptionGroupDTO {
        private Long groupId;
        private String groupName;
        private boolean isRequired;
        private boolean isMultiple;
        private java.util.List<OptionItemDTO> optionItems = new java.util.ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class OptionItemDTO {
        private Long itemId;
        private String itemName;
        private double extraPrice;
    }
}
