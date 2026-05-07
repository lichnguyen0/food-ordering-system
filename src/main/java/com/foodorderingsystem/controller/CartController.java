package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.CartItem;
import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.repository.FoodRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("cart")
public class CartController {

    private final FoodRepository foodRepository;

    public CartController(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @ModelAttribute("cart")
    public Cart cart() {
        return new Cart();
    }


    @GetMapping("/cart/add/{id}")
    public String add(@PathVariable Long id,
                      @ModelAttribute("cart") Cart cart) {

        Food food = foodRepository.findById(id).get();

        CartItem item = cart.getItems().get(id);

        if (item == null) {
            item = new CartItem();
            item.setFoodId(food.getFoodId());
            item.setFoodName(food.getFoodName());
            item.setPrice(food.getPrice());
            item.setQuantity(1);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }

        cart.getItems().put(id, item);

        return "redirect:/foods";
    }
}

