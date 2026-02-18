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
    private String recentRole; // 가장 최근 selected_date의 role (관리자 페이지용)

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

    public String getRecentRole() {
        return recentRole;
    }

    public void setRecentRole(String recentRole) {
        this.recentRole = recentRole;
    }
}

