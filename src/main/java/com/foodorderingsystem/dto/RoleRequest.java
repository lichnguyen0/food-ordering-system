package com.foodorderingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    private Long id;

    @NotBlank(message = "Mã vai trò không được để trống")
    private String code;

    @NotBlank(message = "Tên vai trò không được để trống")
    private String name;

    private String description;

    private Set<Long> permissionIds;
}
