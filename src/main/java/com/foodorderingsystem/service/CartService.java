package com.foodorderingsystem.service;

import com.foodorderingsystem.model.CartItem;
import com.foodorderingsystem.model.Food;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private Map<Long, CartItem> cart = new HashMap<>();

    public void addToCart(Food food) {

        if (cart.containsKey(food.getFoodId())) {
            CartItem item = cart.get(food.getFoodId());
            item.setQuantity(item.getQuantity() + 1);
        } else {
            CartItem item = new CartItem();
            item.setFood(food);
            item.setQuantity(1);
            cart.put(food.getFoodId(), item);
        }
    }

    public Map<Long, CartItem> getCart() {
        return cart;
    }

    public void clear() {
        cart.clear();
    }
}
