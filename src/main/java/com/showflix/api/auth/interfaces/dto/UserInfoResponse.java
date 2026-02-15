package com.showflix.api.auth.interfaces.dto;

/**
 * Interfaces Layer - 사용자 정보 응답 DTO
 */
public class UserInfoResponse {
    private String userid;
    private String username;
    private boolean admin;

    public UserInfoResponse() {
    }

    public UserInfoResponse(String userid, String username, boolean admin) {
        this.userid = userid;
        this.username = username;
        this.admin = admin;
    }

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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
