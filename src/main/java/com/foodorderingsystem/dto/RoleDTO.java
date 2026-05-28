package com.foodorderingsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Set<Long> permissionIds;
}
