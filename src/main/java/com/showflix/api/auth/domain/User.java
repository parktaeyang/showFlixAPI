package com.showflix.api.auth.domain;

/**
 * Domain Layer - User 애그리게이트
 * (DB 엔티티가 아니라 도메인 모델로 사용)
 */
public class User {

    private String userid;
    private String username;
    private String password; // 해시된 비밀번호
    private boolean admin;
    private String accountType; // AccountType.name() 값 (ACTOR, STAFF, CAPTAIN, ADMIN)
    private String role;        // 계정 생성 시 지정된 역할 - ScheduleRole.name() 값 (DOOR, MALE1 등), nullable
    private String recentRole;  // 가장 최근 selected_date의 role (관리자 페이지용)

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRecentRole() {
        return recentRole;
    }

    public void setRecentRole(String recentRole) {
        this.recentRole = recentRole;
    }
}

