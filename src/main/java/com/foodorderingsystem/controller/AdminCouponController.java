package com.foodorderingsystem.controller;

import com.foodorderingsystem.model.Coupon;
import com.foodorderingsystem.repository.CouponRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponRepository couponRepository;

    public AdminCouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Coupon> coupons = couponRepository.findAll();
        model.addAttribute("coupons", coupons);
        return "admin/coupons/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons/form";
    }

    @PostMapping("/add")
    public String saveCoupon(@ModelAttribute Coupon coupon) {
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase().trim());
        }
        couponRepository.save(coupon);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid coupon Id:" + id));
        model.addAttribute("coupon", coupon);
        return "admin/coupons/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCoupon(@PathVariable Long id, @ModelAttribute Coupon coupon) {
        coupon.setCouponId(id);
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase().trim());
        }
        couponRepository.save(coupon);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return "redirect:/admin/coupons";
    }
}
