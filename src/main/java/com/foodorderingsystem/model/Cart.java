package com.foodorderingsystem.model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Long, CartItem> items = new HashMap<>();

    public Map<Long, CartItem> getItems() {
        return items;
    }
}
