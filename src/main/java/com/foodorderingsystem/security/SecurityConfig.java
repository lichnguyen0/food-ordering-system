package com.foodorderingsystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        // ADMIN kế thừa quyền của STAFF, KITCHEN, SHIPPER, USER
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("STAFF")
                .role("ADMIN").implies("KITCHEN")
                .role("ADMIN").implies("SHIPPER")
                .role("STAFF").implies("USER")
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ── Public (không cần đăng nhập) ──────────────────────────────
                .requestMatchers(
                    "/", "/login", "/register",
                    "/css/**", "/js/**", "/images/**", "/uploads/**",
                    "/restaurant/**", "/api/cart/**"
                ).permitAll()

                // ── ADMIN only ─────────────────────────────────────────────────
                .requestMatchers("/admin/users/**").hasRole("ADMIN")

                // ── ADMIN + STAFF (quản lý nội dung & đơn hàng toàn hệ thống) ─
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")

                // ── STAFF dashboard (xử lý đơn hàng) ─────────────────────────
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                // ── KITCHEN dashboard (chế biến món ăn) ───────────────────────
                .requestMatchers("/kitchen/**").hasAnyRole("ADMIN", "KITCHEN")

                // ── SHIPPER dashboard (giao hàng) ──────────────────────────────
                .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")

                // ── CUSTOMER (đặt đơn, xem lịch sử) ──────────────────────────
                .requestMatchers("/cart/**", "/checkout", "/orders", "/order/**").hasRole("USER")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    // Redirect đến đúng dashboard theo role sau khi đăng nhập
                    Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                    if (roles.contains("ROLE_ADMIN")) {
                        response.sendRedirect("/admin");
                    } else if (roles.contains("ROLE_STAFF")) {
                        response.sendRedirect("/staff");
                    } else if (roles.contains("ROLE_KITCHEN")) {
                        response.sendRedirect("/kitchen");
                    } else if (roles.contains("ROLE_SHIPPER")) {
                        response.sendRedirect("/shipper");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe
                .userDetailsService(userDetailsService)
                .key("foodOrderingSystemSecretRememberMeKey")
                .tokenValiditySeconds(86400 * 14) // 14 ngày
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    String refererUrl = request.getHeader("Referer");
                    if (refererUrl != null
                            && !refererUrl.contains("/login")
                            && !refererUrl.contains("/register")
                            && !refererUrl.contains("/admin")
                            && !refererUrl.contains("/staff")
                            && !refererUrl.contains("/kitchen")
                            && !refererUrl.contains("/shipper")) {
                        response.sendRedirect(refererUrl);
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .deleteCookies("remember-me")
                .permitAll()
            );

        return http.build();
    }
}
