package com.showflix.api.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showflix.api.auth.domain.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Infrastructure Layer - 로그인 성공 핸들러
 * 세션에 사용자 정보 저장 및 JSON 응답 반환
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public CustomLoginSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // CustomUserDetails에서 User 객체 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("LOGIN_USER_ID", user.getUserid());
        session.setAttribute("LOGIN_USER_NAME", user.getUsername());
        session.setAttribute("LOGIN_USER_ADMIN", user.isAdmin());
        
        // SecurityContext는 AbstractAuthenticationProcessingFilter가 자동으로 저장하지만,
        // 명시적으로 확인하기 위해 SecurityContextHolder에 설정되어 있는지 확인
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        // JSON 응답으로 redirect 경로 전달
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> data = new HashMap<>();
        data.put("redirect", "/schedule/calendar");

        objectMapper.writeValue(response.getWriter(), data);
    }
}
