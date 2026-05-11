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

    private String description;
    private String image;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @NotNull(message = "Trạng thái không được để trống")
    private FoodStatus status;

    private java.util.List<String> additionalImages = new java.util.ArrayList<>();
}
