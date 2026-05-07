package com.foodorderingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long foodId;
    private int quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "orderId")
    private Order order;
}
