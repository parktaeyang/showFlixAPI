package com.showflix.api.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Infrastructure Layer - 로그인 실패 핸들러
 * 적절한 에러 메시지 반환
 */
@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public CustomLoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // HTTP Status Code 401 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String errMsg;
        
        // 예외 타입에 따른 에러 메시지 설정
        if (exception instanceof BadCredentialsException) {
            errMsg = "아이디 또는 비밀번호가 일치하지 않습니다.";
        } else if (exception instanceof AuthenticationServiceException) {
            errMsg = exception.getMessage();
        } else {
            errMsg = "예상치 못한 오류가 발생하였습니다.\n관리자에게 문의해주세요.";
        }

        // JSON 바디로 errorMessage 전달
        Map<String, String> data = Map.of("errorMessage", errMsg);
        objectMapper.writeValue(response.getWriter(), data);
    }
}
