package com.showflix.api.auth.interfaces.dto;

/**
 * Interfaces Layer - 로그인 요청 DTO
 *
 * 프론트에서 보내는 username을 userid로 사용한다.
 */
public class LoginRequest {

    private String username; // userid로 사용
    private String password;

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
}

