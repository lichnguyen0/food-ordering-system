package com.foodorderingsystem.controller.admin;

import com.foodorderingsystem.model.role.Role;
import com.foodorderingsystem.model.user.User;
import com.foodorderingsystem.model.user.UserRole;
import com.foodorderingsystem.repository.role.RoleRepository;
import com.foodorderingsystem.repository.user.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String search) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.toLowerCase().trim();
            users = userRepository.findAll().stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(keyword)
                            || u.getFullName().toLowerCase().contains(keyword)
                            || u.getEmail().toLowerCase().contains(keyword)
                            || (u.getPhone() != null && u.getPhone().contains(keyword)))
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/users/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng có ID: " + id));
        
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        model.addAttribute("userRoles", UserRole.values());
        
        // Lấy danh sách ID của các Role hiện tại để tiện checked trên form
        Set<Long> assignedRoleIds = user.getRoles() != null 
                ? user.getRoles().stream().map(Role::getId).collect(Collectors.toSet())
                : new HashSet<>();
        model.addAttribute("assignedRoleIds", assignedRoleIds);
        
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam Long userId,
                       @RequestParam UserRole role,
                       @RequestParam(required = false) List<Long> roleIds,
                       RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng có ID: " + userId));
            
            // 1. Cập nhật UserRole enum (Legacy Role)
            user.setRole(role);
            
            // 2. Cập nhật RBAC Roles
            Set<Role> roles = new HashSet<>();
            if (roleIds != null && !roleIds.isEmpty()) {
                roleIds.forEach(id -> roleRepository.findById(id).ifPresent(roles::add));
            } else {
                // Nếu không chọn vai trò RBAC nào, tự động gán tương ứng theo UserRole enum
                String roleCode = "ROLE_" + role.name();
                roleRepository.findByCode(roleCode).ifPresent(roles::add);
            }
            user.setRoles(roles);
            
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
