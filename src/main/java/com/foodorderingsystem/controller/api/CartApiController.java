package com.foodorderingsystem.controller.api;

import com.foodorderingsystem.model.cart.Cart;
import com.foodorderingsystem.model.cart.CartItem;
import com.foodorderingsystem.model.coupon.Coupon;
import com.foodorderingsystem.model.food.Food;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.coupon.CouponRepository;
import com.foodorderingsystem.repository.food.FoodRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final FoodRepository foodRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartApiController(FoodRepository foodRepository, CouponRepository couponRepository, CartService cartService, UserRepository userRepository) {
        this.foodRepository = foodRepository;
        this.couponRepository = couponRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private Cart getCart(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }
        Cart cart = cartService.getOrCreateCart(session, user);
        session.setAttribute("cart", cart);
        return cart;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    @PostMapping("/add/{id}")
    public CartResponse add(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") boolean force, HttpSession session) {
        Cart cart = getCart(session);
        Food food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found"));

        Long currentRestId = cart.getRestaurantId();
        if (currentRestId != null && food.getRestaurant() != null && !currentRestId.equals(food.getRestaurant().getRestaurantId())) {
            if (force) {
                cart = cartService.clearCart(session, getCurrentUser());
            } else {
                return createConflictResponse(cart, cart.getRestaurantName());
            }
        }

        cart = cartService.addToCart(session, getCurrentUser(), id, null, 0, force);
        session.setAttribute("cart", cart);
        return createResponse(cart);
    }

    @PostMapping("/add-with-options/{id}")
    public CartResponse addWithOptions(@PathVariable Long id, @RequestBody AddToCartRequest req, @RequestParam(required = false, defaultValue = "false") boolean force, HttpSession session) {
        Cart cart = getCart(session);
        Food food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found"));

        Long currentRestId = cart.getRestaurantId();
        if (currentRestId != null && food.getRestaurant() != null && !currentRestId.equals(food.getRestaurant().getRestaurantId())) {
            if (force) {
                cart = cartService.clearCart(session, getCurrentUser());
            } else {
                return createConflictResponse(cart, cart.getRestaurantName());
            }
        }

        cart = cartService.addToCart(session, getCurrentUser(), id, req.getOptionsText(), req.getExtraPrice(), force);
        session.setAttribute("cart", cart);
        return createResponse(cart);
    }

    @PostMapping("/increase/{cartItemId}")
    public CartResponse increase(@PathVariable String cartItemId, HttpSession session) {
        Cart cart = getCart(session);
        if (cart.getItems().containsKey(cartItemId)) {
            int currentQty = cart.getItems().get(cartItemId).getQuantity();
            cart = cartService.updateQuantity(session, getCurrentUser(), cartItemId, currentQty + 1);
            session.setAttribute("cart", cart);
        }
        return createResponse(cart);
    }

    @PostMapping("/decrease/{cartItemId}")
    public CartResponse decrease(@PathVariable String cartItemId, HttpSession session) {
        Cart cart = getCart(session);
        if (cart.getItems().containsKey(cartItemId)) {
            int currentQty = cart.getItems().get(cartItemId).getQuantity();
            if (currentQty <= 1) {
                cart = cartService.removeFromCart(session, getCurrentUser(), cartItemId);
            } else {
                cart = cartService.updateQuantity(session, getCurrentUser(), cartItemId, currentQty - 1);
            }
            session.setAttribute("cart", cart);
        }
        return createResponse(cart);
    }

    @PostMapping("/remove/{cartItemId}")
    public CartResponse remove(@PathVariable String cartItemId, HttpSession session) {
        Cart cart = cartService.removeFromCart(session, getCurrentUser(), cartItemId);
        session.setAttribute("cart", cart);
        return createResponse(cart);
    }

    @PostMapping("/decrease-by-food/{foodId}")
    public CartResponse decreaseByFood(@PathVariable Long foodId, HttpSession session) {
        Cart cart = getCart(session);
        String targetCartItemId = null;
        for (String key : cart.getItems().keySet()) {
            if (cart.getItems().get(key).getFood().getFoodId().equals(foodId)) {
                targetCartItemId = key;
                if (key.endsWith("-0")) break;
            }
        }

        if (targetCartItemId != null) {
            int currentQty = cart.getItems().get(targetCartItemId).getQuantity();
            if (currentQty <= 1) {
                cart = cartService.removeFromCart(session, getCurrentUser(), targetCartItemId);
            } else {
                cart = cartService.updateQuantity(session, getCurrentUser(), targetCartItemId, currentQty - 1);
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
        return createResponse(cart, "SUCCESS", null, null);
    }

    private CartResponse createConflictResponse(Cart cart, String conflictRestaurantName) {
        return createResponse(cart, "CONFLICT", "Bạn có muốn tạo giỏ hàng mới? Việc thêm món từ cửa hàng này sẽ xóa các món hiện có trong giỏ hàng từ cửa hàng trước đó.", conflictRestaurantName);
    }

    private CartResponse createResponse(Cart cart,
                                        String status,
                                        String message,
                                        String conflictRestaurantName) {

        Map<Long, Integer> itemQuantities = new HashMap<>();

        List<CartItemDTO> items = new ArrayList<>();

        cart.getItems().forEach((id, item) -> {

            itemQuantities.merge(
                    item.getFood().getFoodId(),
                    item.getQuantity(),
                    Integer::sum
            );

            items.add(new CartItemDTO(
                    item.getCartItemId(),
                    item.getFood().getFoodId(),
                    item.getFood().getFoodName(),
                    item.getUnitPrice(),
                    item.getFood().getImage(),
                    item.getQuantity(),
                    item.getOptionsText()
            ));
        });

        String activeRestaurantName = cart.getRestaurantName();

        return new CartResponse(
                status,
                message,
                conflictRestaurantName,
                activeRestaurantName,
                cart.getTotalQuantity(),
                cart.getTotalPrice(),
                itemQuantities,
                items
        );
    }

    public static class AddToCartRequest {
        private String optionsText;
        private double extraPrice;

        public String getOptionsText() {
            return optionsText;
        }

        public void setOptionsText(String optionsText) {
            this.optionsText = optionsText;
        }

        public double getExtraPrice() {
            return extraPrice;
        }

        public void setExtraPrice(double extraPrice) {
            this.extraPrice = extraPrice;
        }
    }

    public static class CartItemDTO {
        private String cartItemId;
        private Long foodId;
        private String foodName;
        private double price;
        private String image;
        private int quantity;
        private String optionsText;

        public CartItemDTO(String cartItemId, Long foodId, String foodName, double price, String image, int quantity, String optionsText) {
            this.cartItemId = cartItemId;
            this.foodId = foodId;
            this.foodName = foodName;
            this.price = price;
            this.image = image;
            this.quantity = quantity;
            this.optionsText = optionsText;
        }

        public String getCartItemId() {
            return cartItemId;
        }

        public Long getFoodId() {
            return foodId;
        }

        public String getFoodName() {
            return foodName;
        }

        public double getPrice() {
            return price;
        }

        public String getImage() {
            return image;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getOptionsText() {
            return optionsText;
        }
    }

    public static class CartResponse {

        private String status;

        private String message;

        private String conflictRestaurantName;

        private String activeRestaurantName;

        private int totalQuantity;

        private double totalPrice;

        private Map<Long, Integer> itemQuantities;

        private List<CartItemDTO> items;

        public CartResponse(String status,
                            String message,
                            String conflictRestaurantName,
                            String activeRestaurantName,
                            int totalQuantity,
                            double totalPrice,
                            Map<Long, Integer> itemQuantities,
                            List<CartItemDTO> items) {

            this.status = status;
            this.message = message;
            this.conflictRestaurantName = conflictRestaurantName;
            this.activeRestaurantName = activeRestaurantName;
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
            this.itemQuantities = itemQuantities;
            this.items = items;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getConflictRestaurantName() {
            return conflictRestaurantName;
        }

        public String getActiveRestaurantName() {
            return activeRestaurantName;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public Map<Long, Integer> getItemQuantities() {
            return itemQuantities;
        }

        public List<CartItemDTO> getItems() {
            return items;
        }
    }

    @PostMapping("/apply-coupon/{code}")
    public CouponResponse applyCoupon(@PathVariable String code,
                                      HttpSession session) {
        Cart cart = getCart(session);

        Coupon coupon = couponRepository
                .findByCodeIgnoreCaseAndActiveTrue(code)
                .orElse(null);

        if (coupon == null) {
            return new CouponResponse(
                    false,
                    "Mã giảm giá không hợp lệ hoặc đã hết hạn!",
                    0.0
            );
        }

        double subtotal = cart.getTotalPrice();

        if (subtotal < coupon.getMinOrderValue()) {
            return new CouponResponse(
                    false,
                    "Đơn hàng tối thiểu phải đạt "
                            + new DecimalFormat("#,###").format(coupon.getMinOrderValue())
                            + " ₫ để áp dụng mã này!",
                    0.0
            );
        }

        double discountAmount = 0.0;

        if ("PERCENTAGE".equals(coupon.getDiscountType())) {

            discountAmount = subtotal * (coupon.getDiscountValue() / 100.0);

            if (coupon.getMaxDiscountAmount() > 0
                    && discountAmount > coupon.getMaxDiscountAmount()) {
                discountAmount = coupon.getMaxDiscountAmount();
            }

        } else if ("FIXED_AMOUNT".equals(coupon.getDiscountType())) {

            discountAmount = coupon.getDiscountValue();
        }

        if (discountAmount > subtotal) {
            discountAmount = subtotal;
        }

        session.setAttribute("appliedCouponCode", coupon.getCode());

        return new CouponResponse(
                true,
                "Áp dụng mã giảm giá thành công!",
                discountAmount,
                coupon.getCode(),
                coupon.getDescription()
        );
    }

    @PostMapping("/remove-coupon")
    public CouponResponse removeCoupon(HttpSession session) {
        session.removeAttribute("appliedCouponCode");
        return new CouponResponse(true, "Đã hủy áp dụng mã giảm giá!", 0.0);
    }

    @GetMapping("/active-coupons")
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findAllByActiveTrue();
    }

    public static class CouponResponse {
        private boolean success;
        private String message;
        private double discountAmount;
        private String code;
        private String description;

        public CouponResponse(boolean success, String message, double discountAmount) {
            this.success = success;
            this.message = message;
            this.discountAmount = discountAmount;
        }

        public CouponResponse(boolean success, String message, double discountAmount, String code, String description) {
            this.success = success;
            this.message = message;
            this.discountAmount = discountAmount;
            this.code = code;
            this.description = description;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public double getDiscountAmount() {
            return discountAmount;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}