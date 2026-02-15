package com.schedulemng.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		// HTTP Status Code 401 Setting
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		String errMsg;
		// 아이디 혹은 비밀번호 불일치
		if (exception.getClass().getSimpleName().equals("BadCredentialsException")) {
			errMsg = "아이디 또는 비밀번호가 일치하지 않습니다.";
		}
		// 아이디 혹은 비밀번호 미입력
		else if (exception.getClass().getSimpleName().equals("AuthenticationServiceException")) {
			errMsg = exception.getMessage();
		}
		// 그 외 오류
		else {
			errMsg = "예상치 못한 오류가 발생하였습니다.\n관리자에게 문의해주세요.";
		}

		// JSON 바디로 errorMessage 전달
		Map<String, String> data = Map.of("errorMessage", errMsg);
		objectMapper.writeValue(response.getWriter(), data);

	}

}
