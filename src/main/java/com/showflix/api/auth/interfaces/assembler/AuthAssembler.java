package com.showflix.api.auth.interfaces.assembler;

import com.showflix.api.auth.application.command.LoginCommand;
import com.showflix.api.auth.interfaces.dto.LoginRequest;

/**
 * Interfaces Layer - DTO ↔ Command 변환기
 */
public class AuthAssembler {

    private AuthAssembler() {
    }

    public static LoginCommand toCommand(LoginRequest request) {
        // 프론트에서 보내는 username을 userid로 사용
        return new LoginCommand(
                request.getUsername(),
                request.getPassword()
        );
    }
}

