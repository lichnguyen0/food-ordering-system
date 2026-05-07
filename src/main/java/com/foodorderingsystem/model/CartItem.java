package com.foodorderingsystem.model;

import lombok.Data;

@Data
public class CartItem {

    private Long foodId;
    private String foodName;
    private double price;
    private int quantity;
}
