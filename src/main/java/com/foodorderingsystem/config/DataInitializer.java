package com.foodorderingsystem.config;

import com.foodorderingsystem.model.User;
import com.foodorderingsystem.model.UserRole;
import com.foodorderingsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin if not exists
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

        // Create default user if not exists
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
    }
}
