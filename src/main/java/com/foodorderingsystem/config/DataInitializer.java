package com.foodorderingsystem.config; //test nhanh // xoá dc có trog data r

import com.foodorderingsystem.model.coupon.Coupon;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.model.user.UserRole;
import com.foodorderingsystem.repository.coupon.CouponRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
// tạo dữ liệu ban đầu, chạy tự động một lần  ngày lúc ứng dụng khởi động
public class DataInitializer implements CommandLineRunner { //

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CouponRepository couponRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder, 
                           CouponRepository couponRepository,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.couponRepository = couponRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // ── FIX LỖI DATABASE TỰ ĐỘNG ──────────────────────────────────────────
        // Ép kiểu cột role trong MySQL thành VARCHAR(50) để hỗ trợ các role mới dài chữ
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(50)");
            System.out.println("✅ Đã tự động ALTER TABLE users đổi cột role thành VARCHAR(50) thành công!");
        } catch (Exception e) {
            System.out.println("⚠️ Không thể ALTER TABLE users (có thể bảng chưa được tạo hoặc db không hỗ trợ): " + e.getMessage());
        }
        // ──────────────────────────────────────────────────────────────────────

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

        // ── Tạo tài khoản STAFF (nhân viên xử lý đơn) ────────────────────
        if (userRepository.findByUsername("staff").isEmpty()) {
            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setEmail("staff@foodsystem.com");
            staff.setFullName("Nhân Viên Xử Lý Đơn");
            staff.setPhone("0901000001");
            staff.setRole(UserRole.STAFF);
            userRepository.save(staff);
            System.out.println("Staff account created: staff / staff123");
        }

        // ── Tạo tài khoản KITCHEN (nhân viên bếp) ─────────────────────────
        if (userRepository.findByUsername("kitchen").isEmpty()) {
            User kitchen = new User();
            kitchen.setUsername("kitchen");
            kitchen.setPassword(passwordEncoder.encode("kitchen123"));
            kitchen.setEmail("kitchen@foodsystem.com");
            kitchen.setFullName("Nhân Viên Bếp");
            kitchen.setPhone("0901000002");
            kitchen.setRole(UserRole.KITCHEN);
            userRepository.save(kitchen);
            System.out.println("Kitchen account created: kitchen / kitchen123");
        }

        // ── Tạo tài khoản SHIPPER (giao hàng) ─────────────────────────────
        if (userRepository.findByUsername("shipper").isEmpty()) {
            User shipper = new User();
            shipper.setUsername("shipper");
            shipper.setPassword(passwordEncoder.encode("shipper123"));
            shipper.setEmail("shipper@foodsystem.com");
            shipper.setFullName("Shipper Nguyễn Văn A");
            shipper.setPhone("0901000003");
            shipper.setVehicleNumber("51B-12345");
            shipper.setAvailable(true);
            shipper.setRole(UserRole.SHIPPER);
            userRepository.save(shipper);
            System.out.println("Shipper account created: shipper / shipper123");
        }

        // Phiếu giảm giá mặc định của hạt giống
        if (couponRepository.count() == 0) {
            Coupon percentageCoupon = new Coupon();
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

            Coupon fixedCoupon = new Coupon();
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
