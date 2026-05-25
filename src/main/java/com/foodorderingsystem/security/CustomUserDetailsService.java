package com.foodorderingsystem.security;

import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.repository.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
//xử lý đăng nhập (authentication) trong Spring Security
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)  // khi người dùng username hoặc email để đăng nhập
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail) //tìm user trong database
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail))); // nếu khôgn có báo lỗi

        return new CustomUserDetails(user);
    }
}
