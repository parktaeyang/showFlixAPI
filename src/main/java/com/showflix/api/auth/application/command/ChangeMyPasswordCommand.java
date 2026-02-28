package com.showflix.api.auth.application.command;

/**
 * Application Layer - 본인 비밀번호 변경 커맨드
 */
public class ChangeMyPasswordCommand {

    private final String currentPassword;
    private final String newPassword;

    public ChangeMyPasswordCommand(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
