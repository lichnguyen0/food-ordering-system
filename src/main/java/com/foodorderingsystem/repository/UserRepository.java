package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(DISTINCT u.userId) FROM User u JOIN u.orders o WHERE o.orderDate >= :firstDayOfMonth")
    long countActiveUsersThisMonth(@Param("firstDayOfMonth") LocalDateTime firstDayOfMonth);
}
