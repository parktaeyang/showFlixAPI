package com.showflix.api.auth.domain;

/**
 * Domain Layer - 계정유형 Enum
 * DB에는 name() 코드값(ACTOR, STAFF 등)을 VARCHAR로 저장
 */
public enum AccountType {

    ACTOR("배우"),
    STAFF("스텝"),
    CAPTAIN("캡틴"),
    ADMIN("관리자");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
