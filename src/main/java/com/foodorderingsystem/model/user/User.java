package com.foodorderingsystem.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorderingsystem.model.order.Order;
import com.foodorderingsystem.model.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private java.util.Set<com.foodorderingsystem.model.role.Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Order> orders;

    // ── Thông tin bổ sung cho SHIPPER ─────────────────────────────────────
    /** Biển số xe của SHIPPER */
    @Column(length = 20)
    private String vehicleNumber;

    /** SHIPPER có đang sẵn sàng nhận đơn không */
    private Boolean available = true;

    // ── Thông tin bổ sung cho KITCHEN ─────────────────────────────────────
    /** KITCHEN thuộc nhà hàng nào (optional cho hệ thống đa nhà hàng) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "working_restaurant_id")
    @JsonIgnore
    private Restaurant workingRestaurant;
}

