package com.foodorderingsystem.config; //test nhanh // xoá dc có trog data r

import com.foodorderingsystem.model.User;
import com.foodorderingsystem.model.UserRole;
import com.foodorderingsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
// tạo dữ liệu ban đầu, chạy tự động một lần  ngày lúc ứng dụng khởi động
public class DataInitializer implements CommandLineRunner { //

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.foodorderingsystem.repository.CouponRepository couponRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, com.foodorderingsystem.repository.CouponRepository couponRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.couponRepository = couponRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Tạo quản trị viên mặc định nếu không tồn tại
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@foodsystem.com");
            admin.setFullName("Administrator");
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin account created: admin / admin123");
        }

        // Tạo người dùng mặc định nếu không tồn tại
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@foodsystem.com");
            user.setFullName("Default User");
            user.setRole(UserRole.USER);
            userRepository.save(user);
            System.out.println("Default user account created: user / user123");
        }

        // Phiếu giảm giá mặc định của hạt giống
        if (couponRepository.count() == 0) {
            com.foodorderingsystem.model.Coupon percentageCoupon = new com.foodorderingsystem.model.Coupon();
            percentageCoupon.setCode("LUNO20");
            percentageCoupon.setDescription("Giảm 20% tối đa 30K cho đơn từ 50K");
            percentageCoupon.setDiscountType("PERCENTAGE");  // PERCENTAGE kiểu tỉ lệ phần trăm
            percentageCoupon.setDiscountValue(20.0); //đặt giá trị giảm giá
            percentageCoupon.setMinOrderValue(50000.0); // đặt giá trị đơn hàng tối thiểu
            percentageCoupon.setMaxDiscountAmount(30000.0); //đặt số tiền giảm giá tối đa
            percentageCoupon.setExpiryDate(java.time.LocalDate.now().plusMonths(3));// đặt ngày hết hạn
            percentageCoupon.setUsageLimit(500);  //đặt giới hạn sử dụng
            percentageCoupon.setActive(true); //đặt hoạt động
            couponRepository.save(percentageCoupon);

            com.foodorderingsystem.model.Coupon fixedCoupon = new com.foodorderingsystem.model.Coupon();
            fixedCoupon.setCode("FREESHIP");
            fixedCoupon.setDescription("Giảm ngay 16K cho đơn hàng từ 30K");
            fixedCoupon.setDiscountType("FIXED_AMOUNT");
            fixedCoupon.setDiscountValue(16000.0);
            fixedCoupon.setMinOrderValue(30000.0);
            fixedCoupon.setMaxDiscountAmount(16000.0);
            fixedCoupon.setExpiryDate(java.time.LocalDate.now().plusMonths(2));
            fixedCoupon.setUsageLimit(1000);
            fixedCoupon.setActive(true);
            couponRepository.save(fixedCoupon);

            System.out.println("Sample coupons LUNO20 and FREESHIP seeded successfully.");
        }
    }
}
