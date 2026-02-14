package com.showflix.api.auth.application.command;

/**
 * Application Layer - 로그인 유스케이스 입력 모델
 */
public class LoginCommand {

    private final String userid;
    private final String password;

    public LoginCommand(String userid, String password) {
        this.userid = userid;
        this.password = password;
    }

    public String getUserid() {
        return userid;
    }

    public String getPassword() {
        return password;
    }
}

