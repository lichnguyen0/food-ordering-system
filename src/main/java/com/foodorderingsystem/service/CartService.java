package com.foodorderingsystem.service;

import com.foodorderingsystem.model.cart.*;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.cart.CartItemRepository;
import com.foodorderingsystem.repository.cart.CartRepository;
import com.foodorderingsystem.repository.food.FoodRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, FoodRepository foodRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodRepository = foodRepository;
    }

    public Cart getOrCreateCart(HttpSession session, User user) {
        CartEntity cartEntity;
        if (user != null) {
            cartEntity = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE).orElse(null);
            if (cartEntity == null) {
                cartEntity = createNewCart(user.getUserId(), null);
            }
        } else {
            String sessionId = session.getId();
            cartEntity = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
            if (cartEntity == null) {
                cartEntity = createNewCart(null, sessionId);
            }
        }
        return convertToDto(cartEntity);
    }

    @Transactional
    public Cart addToCart(HttpSession session, User user, Long foodId, String optionsText, double extraPrice, boolean force) {
        Cart cart = getOrCreateCart(session, user);
        Food food = foodRepository.findById(foodId).orElseThrow(() -> new RuntimeException("Food not found"));

        Long currentRestId = cart.getRestaurantId();
        if (currentRestId != null && food.getRestaurant() != null && !currentRestId.equals(food.getRestaurant().getRestaurantId())) {
            if (force) {
                cart.clear();
            } else {
                return cart;
            }
        }

        cart.add(food, optionsText, extraPrice);
        return saveCartToDatabase(session, user, cart);
    }

    @Transactional
    public Cart removeFromCart(HttpSession session, User user, String cartItemId) {
        Cart cart = getOrCreateCart(session, user);
        cart.remove(cartItemId);
        return saveCartToDatabase(session, user, cart);
    }

    @Transactional
    public Cart updateQuantity(HttpSession session, User user, String cartItemId, int quantity) {
        Cart cart = getOrCreateCart(session, user);
        cart.updateQuantity(cartItemId, quantity);
        return saveCartToDatabase(session, user, cart);
    }

    @Transactional
    public Cart clearCart(HttpSession session, User user) {
        Cart cart = getOrCreateCart(session, user);
        cart.clear();
        return saveCartToDatabase(session, user, cart);
    }

    @Transactional
    public Cart mergeGuestCartToUser(String sessionId, User user) {
        CartEntity guestCart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
        CartEntity userCart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE).orElse(null);

        if (guestCart != null) {
            if (userCart == null) {
                guestCart.setUser(user);
                guestCart.setSessionId(null);
                guestCart = cartRepository.save(guestCart);
            } else {
                List<CartItemEntity> guestItems = cartItemRepository.findByCartCartId(guestCart.getCartId());
                for (CartItemEntity guestItem : guestItems) {
                    Optional<CartItemEntity> existingItem = cartItemRepository.findByCartCartId(userCart.getCartId()).stream()
                            .filter(item -> item.getFoodId().equals(guestItem.getFoodId()) &&
                                    ((item.getOptionsText() == null && guestItem.getOptionsText() == null) ||
                                            (item.getOptionsText() != null && item.getOptionsText().equals(guestItem.getOptionsText()))))
                            .findFirst();
                    if (existingItem.isPresent()) {
                        CartItemEntity item = existingItem.get();
                        item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                        cartItemRepository.save(item);
                    } else {
                        guestItem.setCart(userCart);
                        cartItemRepository.save(guestItem);
                    }
                }
                guestCart.setStatus(CartStatus.MERGED);
                cartRepository.save(guestCart);
            }
        }

        return convertToDto(cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE).orElse(null));
    }

    private CartEntity createNewCart(Long userId, String sessionId) {
        User user = null;
        if (userId != null) {
            user = new User();
            user.setUserId(userId);
        }
        CartEntity cart = new CartEntity(user, sessionId, null, null);
        return cartRepository.save(cart);
    }

    private Cart saveCartToDatabase(HttpSession session, User user, Cart cart) {
        CartEntity cartEntity;
        if (user != null) {
            cartEntity = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE).orElse(null);
        } else {
            String sessionId = session.getId();
            cartEntity = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
        }

        if (cartEntity == null) {
            cartEntity = createNewCart(user != null ? user.getUserId() : null, user == null ? session.getId() : null);
        }

        cartEntity.setRestaurantId(cart.getRestaurantId());
        cartEntity.setRestaurantName(cart.getRestaurantName());
        cartRepository.save(cartEntity);

        cartItemRepository.deleteByCart(cartEntity);

        for (Map.Entry<String, CartItem> entry : cart.getItems().entrySet()) {
            CartItem item = entry.getValue();
            CartItemEntity itemEntity = new CartItemEntity(
                    cartEntity,
                    item.getFood().getFoodId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getOptionsText(),
                    item.getExtraPrice(),
                    item.getFood().getFoodName(),
                    item.getFood().getImage()
            );
            cartItemRepository.save(itemEntity);
        }

        session.setAttribute("cart", cart);
        return cart;
    }

    private Cart convertToDto(CartEntity cartEntity) {
        if (cartEntity == null) {
            return new Cart();
        }
        Cart cart = new Cart();

        List<CartItemEntity> items = cartItemRepository.findByCartCartId(cartEntity.getCartId());
        for (CartItemEntity itemEntity : items) {
            Food food = foodRepository.findById(itemEntity.getFoodId()).orElse(null);
            if (food != null) {
                CartItem item = new CartItem();
                item.setCartItemId(itemEntity.getFoodId() + "-" + (itemEntity.getOptionsText() != null ? itemEntity.getOptionsText().hashCode() : "0"));
                item.setFood(food);
                item.setQuantity(itemEntity.getQuantity());
                item.setOptionsText(itemEntity.getOptionsText());
                item.setExtraPrice(itemEntity.getExtraPrice());
                cart.getItems().put(item.getCartItemId(), item);
            }
        }

        if (cartEntity.getRestaurantId() != null) {
            cart.setRestaurantId(cartEntity.getRestaurantId());
            cart.setRestaurantName(cartEntity.getRestaurantName());
        }

        return cart;
    }
}