package com.foodorderingsystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

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
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("STAFF")
                .role("STAFF").implies("USER")
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/uploads/**", "/restaurant/**", "/api/cart/**").permitAll()
                .requestMatchers("/admin/category/**", "/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("STAFF") // ADMIN implies STAFF
                .requestMatchers("/cart/**", "/order/checkout").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    var roles = org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                    if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_STAFF")) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe
                .userDetailsService(userDetailsService)
                .key("foodOrderingSystemSecretRememberMeKey")
                .tokenValiditySeconds(86400 * 14) // 14 days
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("remember-me")
                .permitAll()
            );
        
        return http.build();
    }
}
