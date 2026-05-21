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

    public static class OptionGroupDTO {

        private Long groupId;

        private String groupName;

        private boolean isRequired;

        private boolean isMultiple;

        private List<OptionItemDTO> optionItems = new ArrayList<>();

        public OptionGroupDTO() {
        }

        public Long getGroupId() {
            return groupId;
        }

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public void setRequired(boolean required) {
            isRequired = required;
        }

        public boolean isMultiple() {
            return isMultiple;
        }

        public void setMultiple(boolean multiple) {
            isMultiple = multiple;
        }

        public List<OptionItemDTO> getOptionItems() {
            return optionItems;
        }

        public void setOptionItems(List<OptionItemDTO> optionItems) {
            this.optionItems = optionItems;
        }
    }

    public static class OptionItemDTO {

        private Long itemId;

        private String itemName;

        private double extraPrice;

        public OptionItemDTO() {
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public double getExtraPrice() {
            return extraPrice;
        }

        public void setExtraPrice(double extraPrice) {
            this.extraPrice = extraPrice;
        }
    }
}
