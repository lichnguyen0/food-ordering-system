package com.foodorderingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodStatus status; // AVAILABLE / SOLD_OUT

    private String image;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<FoodImage> images = new java.util.ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
}
