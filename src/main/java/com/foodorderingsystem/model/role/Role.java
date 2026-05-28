package com.foodorderingsystem.model.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorderingsystem.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
@ToString(exclude = {"permissions", "users"})
@EqualsAndHashCode(exclude = {"permissions", "users"})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users;
}
