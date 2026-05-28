package com.foodorderingsystem.service.impl;

import com.foodorderingsystem.dto.RoleRequest;
import com.foodorderingsystem.model.role.Permission;
import com.foodorderingsystem.model.role.Role;
import com.foodorderingsystem.repository.role.PermissionRepository;
import com.foodorderingsystem.repository.role.RoleRepository;
import com.foodorderingsystem.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò với ID: " + id));
    }

    @Override
    public Role saveRole(RoleRequest roleRequest) {
        Role role;
        if (roleRequest.getId() != null) {
            role = getRoleById(roleRequest.getId());
            // Không cho phép đổi mã (code) của các role hệ thống mặc định để tránh lỗi
            if (isSystemRole(role.getCode())) {
                // Giữ nguyên code cũ
            } else {
                role.setCode(roleRequest.getCode().trim().toUpperCase());
            }
        } else {
            role = new Role();
            String code = roleRequest.getCode().trim().toUpperCase();
            if (!code.startsWith("ROLE_")) {
                code = "ROLE_" + code;
            }
            role.setCode(code);
        }

        role.setName(roleRequest.getName().trim());
        role.setDescription(roleRequest.getDescription() != null ? roleRequest.getDescription().trim() : null);

        // Map Permissions
        Set<Permission> permissions = new HashSet<>();
        if (roleRequest.getPermissionIds() != null) {
            for (Long pId : roleRequest.getPermissionIds()) {
                permissionRepository.findById(pId).ifPresent(permissions::add);
            }
        }
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        if (isSystemRole(role.getCode())) {
            throw new IllegalArgumentException("Không thể xóa vai trò mặc định của hệ thống: " + role.getName());
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    private boolean isSystemRole(String code) {
        if (code == null) return false;
        String upperCode = code.toUpperCase();
        return upperCode.equals("ROLE_ADMIN") ||
               upperCode.equals("ROLE_STAFF") ||
               upperCode.equals("ROLE_KITCHEN") ||
               upperCode.equals("ROLE_SHIPPER") ||
               upperCode.equals("ROLE_USER");
    }
}
