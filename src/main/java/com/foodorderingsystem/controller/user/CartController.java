package com.foodorderingsystem.controller.user;

import com.foodorderingsystem.model.cart.Cart;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.repository.food.FoodRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("cart")
public class CartController {

    private final FoodRepository foodRepository;
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(FoodRepository foodRepository, CartService cartService, UserRepository userRepository) {
        this.foodRepository = foodRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("cart")
    public Cart cart() {
        return new Cart();
    }

    private com.foodorderingsystem.model.user.User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    @GetMapping("/cart/add/{id}")
    public String add(@PathVariable Long id,
                      @ModelAttribute("cart") Cart cart,
                      HttpSession session) {

        Food food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found"));
        com.foodorderingsystem.model.user.User user = getCurrentUser();

        cart = cartService.addToCart(session, user, id, null, 0, false);

        return "redirect:/foods";
    }
}