package com.foodorderingsystem.model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<String, CartItem> items = new HashMap<>();

    public void add(Food food, String optionsText, double extraPrice) {
        String cartItemId = food.getFoodId() + "-" + (optionsText != null ? optionsText.hashCode() : "0");
        CartItem item = items.get(cartItemId);
        if (item == null) {
            item = new CartItem();
            item.setCartItemId(cartItemId);
            item.setFood(food);
            item.setQuantity(1);
            item.setOptionsText(optionsText);
            item.setExtraPrice(extraPrice);
            items.put(cartItemId, item);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    // Quá tải cho các mặt hàng cơ bản không có tùy chọn
    public void add(Food food) {
        add(food, null, 0);
    }

    public void remove(String cartItemId) {
        items.remove(cartItemId);
    }

    public void updateQuantity(String cartItemId, int quantity) {
        if (items.containsKey(cartItemId)) {
            if (quantity <= 0) {
                items.remove(cartItemId);
            } else {
                items.get(cartItemId).setQuantity(quantity);
            }
        }
    }

    public double getTotalPrice() {
         return items.values().stream()
                 .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
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

    public Map<String, CartItem> getItems() {
        return items;
    }

    public Long getRestaurantId() {
        if (items.isEmpty()) {
            return null;
        }
        CartItem firstItem = items.values().iterator().next();
        if (firstItem.getFood() != null && firstItem.getFood().getRestaurant() != null) {
            return firstItem.getFood().getRestaurant().getRestaurantId();
        }
        return null;
    }

    public String getRestaurantName() {
        if (items.isEmpty()) {
            return null;
        }
        CartItem firstItem = items.values().iterator().next();
        if (firstItem.getFood() != null && firstItem.getFood().getRestaurant() != null) {
            return firstItem.getFood().getRestaurant().getName();
        }
        return null;
    }
}
