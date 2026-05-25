package com.foodorderingsystem.controller.user;

import com.foodorderingsystem.model.cart.Cart;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.repository.food.FoodRepository;
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

        Food food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found"));
        cart.add(food);

        return "redirect:/foods";
    }

}

