package com.foodorderingsystem.repository.cart;

import com.foodorderingsystem.model.cart.CartEntity;
import com.foodorderingsystem.model.cart.CartStatus;
import com.foodorderingsystem.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserUserIdAndStatus(Long userId, CartStatus status);

    Optional<CartEntity> findBySessionIdAndStatus(String sessionId, CartStatus status);

    Optional<CartEntity> findByUserAndStatus(User user, CartStatus status);
}