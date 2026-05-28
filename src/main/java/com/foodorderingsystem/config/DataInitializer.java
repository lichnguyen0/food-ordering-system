package com.foodorderingsystem.config;

import com.foodorderingsystem.model.coupon.Coupon;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.model.user.UserRole;
import com.foodorderingsystem.model.role.Permission;
import com.foodorderingsystem.model.role.Role;
import com.foodorderingsystem.repository.coupon.CouponRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import com.foodorderingsystem.repository.role.PermissionRepository;
import com.foodorderingsystem.repository.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CouponRepository couponRepository;
    private final JdbcTemplate jdbcTemplate;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder, 
                           CouponRepository couponRepository,
                           JdbcTemplate jdbcTemplate,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.couponRepository = couponRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    private Permission getOrCreatePermission(String code, String name) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            p.setName(name);
            return permissionRepository.save(p);
        });
    }

    private Role getOrCreateRole(String code, String name, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByCode(code).orElseGet(() -> {
            Role r = new Role();
            r.setCode(code);
            r.setName(name);
            r.setDescription(description);
            return r;
        });
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Override
    public void run(String... args) throws Exception {
        // ── PHÁT HIỆN & DỌN DẸP LỖI CẤU TRÚC BẢNG ROLES/PERMISSIONS CŨ ────────
        try {
            boolean rolesTableExists = false;
            try {
                jdbcTemplate.execute("SELECT 1 FROM roles LIMIT 1");
                rolesTableExists = true;
            } catch (Exception t) {
                // Bảng roles chưa tồn tại hoặc rỗng không truy cập được -> Hibernate tự tạo
            }

            if (rolesTableExists) {
                try {
                    jdbcTemplate.execute("SELECT id FROM roles LIMIT 1");
                } catch (Exception colEx) {
                    System.out.println("⚠️ PHÁT HIỆN BẢNG ROLES CŨ KHÔNG TƯƠNG THÍCH (THIẾU CỘT 'id'). Tiến hành dọn dẹp các bảng cũ để Hibernate khởi tạo lại...");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS user_roles");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS role_permissions");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS roles");
                    jdbcTemplate.execute("DROP TABLE IF EXISTS permissions");
                    System.out.println("✅ ĐÃ DỌN DẸP SẠCH CÁC BẢNG CŨ THÀNH CÔNG! HỆ THỐNG SẼ TỰ ĐỘNG KHỞI TẠO LẠI BẢNG CHUẨN!");
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Bỏ qua kiểm tra bảng roles cũ: " + e.getMessage());
        }
        // ──────────────────────────────────────────────────────────────────────

        // ── FIX LỖI DATABASE TỰ ĐỘNG ──────────────────────────────────────────
        // Ép kiểu cột role trong MySQL thành VARCHAR(50) để hỗ trợ các role mới dài chữ
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(50)");
            System.out.println("✅ Đã tự động ALTER TABLE users đổi cột role thành VARCHAR(50) thành công!");
        } catch (Exception e) {
            System.out.println("⚠️ Không thể ALTER TABLE users (có thể bảng chưa được tạo hoặc db không hỗ trợ): " + e.getMessage());
        }
        // ──────────────────────────────────────────────────────────────────────

        // ── SEED QUYỀN HẠN (PERMISSIONS) ──────────────────────────────────────
        Permission viewDashboard = getOrCreatePermission("VIEW_DASHBOARD", "Xem bảng điều khiển");
        Permission manageFood = getOrCreatePermission("MANAGE_FOOD", "Quản lý món ăn & danh mục");
        Permission manageRestaurant = getOrCreatePermission("MANAGE_RESTAURANT", "Quản lý nhà hàng");
        Permission manageMarketing = getOrCreatePermission("MANAGE_MARKETING", "Quản lý marketing & khuyến mãi");
        Permission manageOrders = getOrCreatePermission("MANAGE_ORDERS", "Quản lý đơn hàng");
        Permission manageInvoices = getOrCreatePermission("MANAGE_INVOICES", "Quản lý hóa đơn");
        Permission manageUsers = getOrCreatePermission("MANAGE_USERS", "Quản lý khách hàng & người dùng");
        Permission manageRoles = getOrCreatePermission("MANAGE_ROLES", "Quản lý vai trò & phân quyền");
        Permission prepareFood = getOrCreatePermission("PREPARE_FOOD", "Chế biến món ăn (Bếp)");
        Permission deliverOrder = getOrCreatePermission("DELIVER_ORDER", "Giao hàng (Shipper)");

        // ── SEED VAI TRÒ (ROLES) ──────────────────────────────────────────────
        // 1. ROLE_ADMIN: Tất cả các quyền
        Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
        Role adminRole = getOrCreateRole("ROLE_ADMIN", "Quản trị viên", "Quản trị toàn hệ thống", adminPermissions);

        // 2. ROLE_STAFF: Quản lý món ăn, danh mục, đơn hàng, hóa đơn, marketing...
        Set<Permission> staffPermissions = Set.of(viewDashboard, manageFood, manageRestaurant, manageMarketing, manageOrders, manageInvoices);
        Role staffRole = getOrCreateRole("ROLE_STAFF", "Nhân viên vận hành", "Xác nhận & xử lý đơn hàng, quản lý nội dung", staffPermissions);

        // 3. ROLE_KITCHEN: Xem dashboard, chế biến món ăn
        Set<Permission> kitchenPermissions = Set.of(viewDashboard, prepareFood);
        Role kitchenRole = getOrCreateRole("ROLE_KITCHEN", "Nhân viên bếp", "Nhận đơn hàng & chế biến món ăn", kitchenPermissions);

        // 4. ROLE_SHIPPER: Xem dashboard, giao nhận đơn hàng
        Set<Permission> shipperPermissions = Set.of(viewDashboard, deliverOrder);
        Role shipperRole = getOrCreateRole("ROLE_SHIPPER", "Nhân viên giao hàng", "Nhận đơn hàng & giao tới khách hàng", shipperPermissions);

        // 5. ROLE_USER: Vai trò khách hàng mặc định
        Set<Permission> userPermissions = new HashSet<>();
        Role userRole = getOrCreateRole("ROLE_USER", "Khách hàng", "Khách hàng mua sắm trên hệ thống", userPermissions);

        // ── SEED TÀI KHOẢN MẶC ĐỊNH (USERS & USER_ROLES) ─────────────────────
        // 1. Quản trị viên (admin)
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@foodsystem.com");
            admin.setFullName("Administrator");
            admin.setRole(UserRole.ADMIN);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("Default admin account created: admin / admin123");
        } else {
            userRepository.findByUsername("admin").ifPresent(u -> {
                if (u.getRoles() == null || u.getRoles().isEmpty()) {
                    u.setRoles(Set.of(adminRole));
                    userRepository.save(u);
                }
            });
        }

        // 2. Khách hàng mặc định (user)
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@foodsystem.com");
            user.setFullName("Default User");
            user.setRole(UserRole.USER);
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
            System.out.println("Default user account created: user / user123");
        } else {
            userRepository.findByUsername("user").ifPresent(u -> {
                if (u.getRoles() == null || u.getRoles().isEmpty()) {
                    u.setRoles(Set.of(userRole));
                    userRepository.save(u);
                }
            });
        }

        // 3. Nhân viên xử lý đơn (staff)
        if (userRepository.findByUsername("staff").isEmpty()) {
            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setEmail("staff@foodsystem.com");
            staff.setFullName("Nhân Viên Xử Lý Đơn");
            staff.setPhone("0901000001");
            staff.setRole(UserRole.STAFF);
            staff.setRoles(Set.of(staffRole));
            userRepository.save(staff);
            System.out.println("Staff account created: staff / staff123");
        } else {
            userRepository.findByUsername("staff").ifPresent(u -> {
                if (u.getRoles() == null || u.getRoles().isEmpty()) {
                    u.setRoles(Set.of(staffRole));
                    userRepository.save(u);
                }
            });
        }

        // 4. Nhân viên bếp (kitchen)
        if (userRepository.findByUsername("kitchen").isEmpty()) {
            User kitchen = new User();
            kitchen.setUsername("kitchen");
            kitchen.setPassword(passwordEncoder.encode("kitchen123"));
            kitchen.setEmail("kitchen@foodsystem.com");
            kitchen.setFullName("Nhân Viên Bếp");
            kitchen.setPhone("0901000002");
            kitchen.setRole(UserRole.KITCHEN);
            kitchen.setRoles(Set.of(kitchenRole));
            userRepository.save(kitchen);
            System.out.println("Kitchen account created: kitchen / kitchen123");
        } else {
            userRepository.findByUsername("kitchen").ifPresent(u -> {
                if (u.getRoles() == null || u.getRoles().isEmpty()) {
                    u.setRoles(Set.of(kitchenRole));
                    userRepository.save(u);
                }
            });
        }

        // 5. Shipper giao hàng (shipper)
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
            shipper.setRoles(Set.of(shipperRole));
            userRepository.save(shipper);
            System.out.println("Shipper account created: shipper / shipper123");
        } else {
            userRepository.findByUsername("shipper").ifPresent(u -> {
                if (u.getRoles() == null || u.getRoles().isEmpty()) {
                    u.setRoles(Set.of(shipperRole));
                    userRepository.save(u);
                }
            });
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
