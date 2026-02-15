package com.schedulemng.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedulemng.entity.User;
import com.schedulemng.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // User 객체 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserid());
        session.setAttribute("isAdmin", user.isAdmin());
        session.setAttribute("role", user.getRole());
        session.setAttribute("accountType", user.getAccountType());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("phoneNumber", user.getPhoneNumber());

        // 2) JSON 응답으로 redirect 경로 전달
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> data = new HashMap<>();
        data.put("redirect", "/schedule/calendar");  // 기본 페이지

        // ObjectMapper 로 JSON 쓰기
        objectMapper.writeValue(response.getWriter(), data);
    }
}
