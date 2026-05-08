package com.foodorderingsystem.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItem {
    private Food food;
    private int quantity;
}
