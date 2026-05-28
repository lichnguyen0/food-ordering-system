package com.foodorderingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FoodOrderingSystemApplication {

    public static void main(String[] args) {
        // ── 🛡️ DỌN DẸP DATABASE CŨ TRƯỚC KHI KHỞI ĐỘNG SPRING BOOT ────────────────
        java.util.Properties props = new java.util.Properties();
        try {
            // Thử load từ resource stream
            try (java.io.InputStream is = FoodOrderingSystemApplication.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (is != null) {
                    props.load(is);
                } else {
                    // Thử load trực tiếp từ file system
                    try (java.io.FileInputStream fis = new java.io.FileInputStream("src/main/resources/application.properties")) {
                        props.load(fis);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Không thể load application.properties để dọn dẹp trước: " + e.getMessage());
        }

        String dbUrl = props.getProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/food_ordering_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        String dbUser = props.getProperty("spring.datasource.username", "root");
        String dbPass = props.getProperty("spring.datasource.password", "123456");

        try {
            // Đăng ký Driver MySQL thủ công để chắc chắn driver được load
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                boolean needDrop = false;
                try (java.sql.Statement stmt = conn.createStatement()) {
                    // 1. Kiểm tra xem bảng roles có tồn tại không
                    boolean rolesTableExists = false;
                    try {
                        stmt.execute("SELECT 1 FROM roles LIMIT 1");
                        rolesTableExists = true;
                    } catch (java.sql.SQLException t) {
                        // Bảng chưa tồn tại -> Hibernate tự sinh
                    }

                    if (rolesTableExists) {
                        // 2. Kiểm tra xem có cột 'id' hay không
                        try {
                            stmt.execute("SELECT id FROM roles LIMIT 1");
                        } catch (java.sql.SQLException colEx) {
                            String errorMsg = colEx.getMessage() != null ? colEx.getMessage().toLowerCase() : "";
                            if (errorMsg.contains("unknown column") || errorMsg.contains("id")) {
                                needDrop = true;
                            }
                        }
                    }

                    if (needDrop) {
                        System.out.println("⚠️ [MAIN] PHÁT HIỆN BẢNG ROLES CŨ THIẾU CỘT 'id'. Tiến hành dọn dẹp bảng cũ...");
                        stmt.execute("DROP TABLE IF EXISTS user_roles");
                        stmt.execute("DROP TABLE IF EXISTS role_permissions");
                        stmt.execute("DROP TABLE IF EXISTS roles");
                        stmt.execute("DROP TABLE IF EXISTS permissions");
                        System.out.println("✅ [MAIN] ĐÃ DỌN DẸP SẠCH CÁC BẢNG CŨ THÀNH CÔNG!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ [MAIN] Bỏ qua kiểm tra dọn dẹp DB cũ: " + e.getMessage());
            e.printStackTrace();
        }
        // ────────────────────────────────────────────────────────────────────────

        SpringApplication.run(FoodOrderingSystemApplication.class, args);
    }

}
