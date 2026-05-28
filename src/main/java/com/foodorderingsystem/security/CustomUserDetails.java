package com.foodorderingsystem.security;

import com.foodorderingsystem.model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        java.util.Set<SimpleGrantedAuthority> authorities = new java.util.HashSet<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (com.foodorderingsystem.model.role.Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.getCode()));
                if (role.getPermissions() != null) {
                    for (com.foodorderingsystem.model.role.Permission p : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(p.getCode()));
                    }
                }
            }
        } else if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}