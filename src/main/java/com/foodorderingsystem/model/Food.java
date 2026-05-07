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

    private String foodName;
    private double price;
    private String Description;
    private String status;// AVAILABLE / SOLD_OUT
    private String image;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
}
