package com.foodorderingsystem.model.cart;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private CartEntity cart;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "options_text", columnDefinition = "TEXT")
    private String optionsText;

    @Column(name = "extra_price")
    private double extraPrice = 0.0;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "food_image")
    private String foodImage;

    public CartItemEntity(CartEntity cart, Long foodId, int quantity, double unitPrice, String optionsText, double extraPrice, String foodName, String foodImage) {
        this.cart = cart;
        this.foodId = foodId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.optionsText = optionsText;
        this.extraPrice = extraPrice;
        this.foodName = foodName;
        this.foodImage = foodImage;
    }
}