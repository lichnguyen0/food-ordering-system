package com.foodorderingsystem.dto;

import com.foodorderingsystem.model.FoodStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor

public class FoodDTO {

    private Long foodId;

    @NotBlank(message = "Tên món ăn không được để trống")
    private String foodName;

    @Positive(message = "Giá phải lớn hơn 0")
    private double price;

    private Double discountPrice;

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

    private List<String> additionalImages = new ArrayList<>();

    private List<OptionGroupDTO> optionGroups = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class OptionGroupDTO {

        private Long groupId;

        private String groupName;

        private boolean required;

        private boolean multiple;

        private List<OptionItemDTO> optionItems = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class OptionItemDTO {

        private Long itemId;

        private String itemName;

        private double extraPrice;
    }
}
