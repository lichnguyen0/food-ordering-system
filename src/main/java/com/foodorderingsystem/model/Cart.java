package com.foodorderingsystem.model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Long, CartItem> items = new HashMap<>();

    public void add(Food food) {
        CartItem item = items.get(food.getFoodId());
        if (item == null) {
            item = new CartItem();
            item.setFood(food);
            item.setQuantity(1);
            items.put(food.getFoodId(), item);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    public void remove(Long foodId) {
        items.remove(foodId);
    }

    public void updateQuantity(Long foodId, int quantity) {
        if (items.containsKey(foodId)) {
            if (quantity <= 0) {
                items.remove(foodId);
            } else {
                items.get(foodId).setQuantity(quantity);
            }
        }
    }

    public double getTotalPrice() {
         return items.values().stream()
                 .mapToDouble(item -> item.getFood().getActivePrice() * item.getQuantity())
                 .sum();
     }

    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clear() {
        items.clear();
    }

    public Map<Long, CartItem> getItems() {
        return items;
    }
}
