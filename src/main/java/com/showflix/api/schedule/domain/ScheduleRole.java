package com.showflix.api.schedule.domain;

/**
 * 스케줄 역할 Enum
 * 레거시 User.Role 기반
 * DB에는 name() 코드값(DOOR, MALE1 등)을 VARCHAR로 저장
 */
public enum ScheduleRole {

    // 스텝 역할
    DOOR("도어"),
    HOLEMAN("홀맨"),
    OPER("오퍼"),
    HELPER("헬퍼"),
    KITCHEN("주방"),

    // 배우 역할
    MALE1("남1"),
    MALE2("남2"),
    MALE3("남3"),
    FEMALE1("여1"),
    FEMALE2("여2"),
    FEMALE3("여3");

    private final String displayName;

    ScheduleRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
