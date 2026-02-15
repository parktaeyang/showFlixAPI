package com.schedulemng.dto;

import com.schedulemng.entity.User;

public record UserDto(
        String userid,
        String username,
        String phoneNumber,
        AccountTypeDto accountType,
        RoleDto role,
        String createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getUserid(),
                user.getUsername(),
                user.getPhoneNumber(),
                AccountTypeDto.from(user.getAccountType()),
                RoleDto.from(user.getRole()),
                user.getCreatedAt()
        );
    }
}
