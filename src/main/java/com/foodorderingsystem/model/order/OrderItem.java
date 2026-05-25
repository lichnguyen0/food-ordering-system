package com.foodorderingsystem.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.food.Food;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "foodId")
    private Food food;

    private int quantity;
    private double price;
    
    @Column(columnDefinition = "TEXT")
    private String optionsText;

    @ManyToOne
    @JoinColumn(name = "orderId")
    @JsonIgnore
    private Order order;
}
