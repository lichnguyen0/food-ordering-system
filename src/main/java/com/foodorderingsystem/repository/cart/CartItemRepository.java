package com.foodorderingsystem.repository.cart;

import com.foodorderingsystem.model.cart.CartEntity;
import com.foodorderingsystem.model.cart.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByCartCartId(Long cartId);

    void deleteByCartCartId(Long cartId);

    void deleteByCart(CartEntity cart);
}