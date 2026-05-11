package com.foodorderingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class FoodImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "foodId")
    private Food food;

    public FoodImage(String imageUrl, Food food) {
        this.imageUrl = imageUrl;
        this.food = food;
    }
}
