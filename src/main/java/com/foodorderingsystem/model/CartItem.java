package com.foodorderingsystem.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItem {
    private String cartItemId;
    private Food food;
    private int quantity;
    private String optionsText;
    private double extraPrice;

    public double getUnitPrice() {
        return (food != null ? food.getActivePrice() : 0) + extraPrice;
    }
}
