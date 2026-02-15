package com.schedulemng.dto;

import com.schedulemng.entity.User;

public record AccountTypeDto(String name, String displayName) {
    public static AccountTypeDto from(User.AccountType accountType) {
        if (accountType == null) return null;
        return new AccountTypeDto(accountType.name(), accountType.getDisplayName());
    }
}