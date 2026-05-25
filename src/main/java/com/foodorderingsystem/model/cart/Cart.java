package com.foodorderingsystem.model.cart;

import com.foodorderingsystem.model.food.Food;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<String, CartItem> items = new HashMap<>();
    private Long restaurantId;
    private String restaurantName;

    public void add(Food food, String optionsText, double extraPrice) {
        if (items.isEmpty() && food.getRestaurant() != null) {
            this.restaurantId = food.getRestaurant().getRestaurantId();
            this.restaurantName = food.getRestaurant().getName();
        }
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
        if (items.isEmpty()) {
            this.restaurantId = null;
            this.restaurantName = null;
        }
    }

    public void updateQuantity(String cartItemId, int quantity) {
        if (items.containsKey(cartItemId)) {
            if (quantity <= 0) {
                items.remove(cartItemId);
                if (items.isEmpty()) {
                    this.restaurantId = null;
                    this.restaurantName = null;
                }
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
        this.restaurantId = null;
        this.restaurantName = null;
    }

    public Map<String, CartItem> getItems() {
        return items;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public String getRestaurantName() {
        return this.restaurantName;
    }
}
