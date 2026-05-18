package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Cart;
import com.foodorderingsystem.model.Food;
import com.foodorderingsystem.repository.FoodRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final FoodRepository foodRepository;

    public CartApiController(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/add/{id}")
    public CartResponse add(@PathVariable Long id, HttpSession session) {
        Cart cart = getCart(session);
        Food food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found"));
        cart.add(food);
        session.setAttribute("cart", cart);
        return createResponse(cart);
    }

    @PostMapping("/decrease/{id}")
    public CartResponse decrease(@PathVariable Long id, HttpSession session) {
        Cart cart = getCart(session);
        if (cart.getItems().containsKey(id)) {
            int currentQty = cart.getItems().get(id).getQuantity();
            if (currentQty <= 1) {
                cart.remove(id);
            } else {
                cart.updateQuantity(id, currentQty - 1);
            }
            session.setAttribute("cart", cart);
        }
        return createResponse(cart);
    }

    @GetMapping("/status")
    public CartResponse status(HttpSession session) {
        Cart cart = getCart(session);
        return createResponse(cart);
    }

    private CartResponse createResponse(Cart cart) {
        java.util.Map<Long, Integer> itemQuantities = new java.util.HashMap<>();
        java.util.List<CartItemDTO> items = new java.util.ArrayList<>();
        
        cart.getItems().forEach((id, item) -> {
            itemQuantities.put(id, item.getQuantity());
            items.add(new CartItemDTO(
                item.getFood().getFoodId(),
                item.getFood().getFoodName(),
                item.getFood().getPrice(),
                item.getFood().getImage(),
                item.getQuantity()
            ));
        });
        
        return new CartResponse(cart.getTotalQuantity(), cart.getTotalPrice(), itemQuantities, items);
    }

    public static class CartItemDTO {
        private Long foodId;
        private String foodName;
        private double price;
        private String image;
        private int quantity;

        public CartItemDTO(Long foodId, String foodName, double price, String image, int quantity) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.price = price;
            this.image = image;
            this.quantity = quantity;
        }

        public Long getFoodId() { return foodId; }
        public String getFoodName() { return foodName; }
        public double getPrice() { return price; }
        public String getImage() { return image; }
        public int getQuantity() { return quantity; }
    }

    public static class CartResponse {
        private int totalQuantity;
        private double totalPrice;
        private java.util.Map<Long, Integer> itemQuantities;
        private java.util.List<CartItemDTO> items;

        public CartResponse(int totalQuantity, double totalPrice, java.util.Map<Long, Integer> itemQuantities, java.util.List<CartItemDTO> items) {
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
            this.itemQuantities = itemQuantities;
            this.items = items;
        }

        public int getTotalQuantity() { return totalQuantity; }
        public double getTotalPrice() { return totalPrice; }
        public java.util.Map<Long, Integer> getItemQuantities() { return itemQuantities; }
        public java.util.List<CartItemDTO> getItems() { return items; }
    }
}
