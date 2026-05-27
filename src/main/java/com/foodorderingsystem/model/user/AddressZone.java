package com.foodorderingsystem.model.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "address_zones")
public class AddressZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ward;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Double baseFee;

    @Column(nullable = false)
    private Double kmRate;

    public Double calculateFee(Double distance) {
        if (distance == null || distance <= 0) {
            return baseFee;
        }
        return baseFee + (distance * kmRate);
    }
}