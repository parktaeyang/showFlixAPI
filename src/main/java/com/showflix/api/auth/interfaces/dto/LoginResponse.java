package com.showflix.api.auth.interfaces.dto;

/**
 * Interfaces Layer - 로그인 성공 응답 DTO
 */
public class LoginResponse {

    private String redirect;

    public LoginResponse(String redirect) {
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}

