package com.showflix.api.auth.interfaces;

import com.showflix.api.auth.application.AuthService;
import com.showflix.api.auth.application.InvalidLoginException;
import com.showflix.api.auth.application.command.LoginCommand;
import com.showflix.api.auth.interfaces.assembler.AuthAssembler;
import com.showflix.api.auth.interfaces.dto.LoginRequest;
import com.showflix.api.auth.interfaces.dto.LoginResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Interfaces Layer - 로그인 HTTP 엔드포인트
 *
 * React 로그인 페이지에서 /auth/login 으로 폼 전송
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * application/x-www-form-urlencoded 로 들어오는 로그인 요청 처리
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(LoginRequest request, HttpSession session) {
        LoginCommand command = AuthAssembler.toCommand(request);

        try {
            AuthService.LoginResult result = authService.login(command);

            // 간단한 세션 기반 로그인 정보 저장
            session.setAttribute("LOGIN_USER_ID", result.getUserid());
            session.setAttribute("LOGIN_USER_NAME", result.getUsername());
            session.setAttribute("LOGIN_USER_ADMIN", result.isAdmin());

            return ResponseEntity.ok(new LoginResponse("/schedule/calendar"));
        } catch (InvalidLoginException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errorMessage", e.getMessage()));
        }
    }
}

