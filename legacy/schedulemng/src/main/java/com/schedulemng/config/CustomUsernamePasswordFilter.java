package com.schedulemng.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomUsernamePasswordFilter extends UsernamePasswordAuthenticationFilter {

	public CustomUsernamePasswordFilter(AuthenticationManager authManager) {
		super.setAuthenticationManager(authManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
			HttpServletResponse res) {
		String username = obtainUsername(req);
		String password = obtainPassword(req);

		if (username == null || username.trim().isEmpty()) {
			throw new AuthenticationServiceException("아이디를 입력해주세요.");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new AuthenticationServiceException("비밀번호를 입력해주세요.");
		}

		return super.attemptAuthentication(req, res);
	}

}
