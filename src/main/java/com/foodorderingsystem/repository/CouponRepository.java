package com.foodorderingsystem.repository;

import com.foodorderingsystem.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository

// kho lưu trữ phiếu giảm giá
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCaseAndActiveTrue(String code);
    List<Coupon> findAllByActiveTrue();
}
