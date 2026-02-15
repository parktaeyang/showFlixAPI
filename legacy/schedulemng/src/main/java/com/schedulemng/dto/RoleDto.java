package com.schedulemng.dto;

import com.schedulemng.entity.User;

public record RoleDto(String name, String displayName) {
    public static RoleDto from(User.Role role) {
        if (role == null) return null;
        return new RoleDto(role.name(), role.getDisplayName());
    }
} 