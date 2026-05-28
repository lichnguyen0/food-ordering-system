package com.foodorderingsystem.controller.admin;

import com.foodorderingsystem.dto.RoleRequest;
import com.foodorderingsystem.model.role.Permission;
import com.foodorderingsystem.model.role.Role;
import com.foodorderingsystem.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

    private final RoleService roleService;

    public AdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/roles/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setPermissionIds(new HashSet<>());
        model.addAttribute("roleRequest", roleRequest);
        model.addAttribute("allPermissions", roleService.getAllPermissions());
        return "admin/roles/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id);
        
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(role.getId());
        roleRequest.setCode(role.getCode());
        roleRequest.setName(role.getName());
        roleRequest.setDescription(role.getDescription());
        
        Set<Long> permissionIds = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        roleRequest.setPermissionIds(permissionIds);

        model.addAttribute("roleRequest", roleRequest);
        model.addAttribute("allPermissions", roleService.getAllPermissions());
        return "admin/roles/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("roleRequest") RoleRequest roleRequest,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("allPermissions", roleService.getAllPermissions());
            return "admin/roles/form";
        }

        try {
            roleService.saveRole(roleRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/roles";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa vai trò: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}
