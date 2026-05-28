package com.foodorderingsystem.service;

import com.foodorderingsystem.dto.RoleRequest;
import com.foodorderingsystem.model.role.Permission;
import com.foodorderingsystem.model.role.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role saveRole(RoleRequest roleRequest);
    void deleteRole(Long id);
    List<Permission> getAllPermissions();
}
