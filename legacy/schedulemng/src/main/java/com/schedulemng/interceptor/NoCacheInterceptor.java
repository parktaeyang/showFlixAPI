package com.schedulemng.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NoCacheInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {

		// 캐시 방지 헤더 설정 (로그인에서 사용: 메인화면에서 뒤로가기 방지)
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
		response.setHeader("Pragma",        "no-cache");
		response.setDateHeader("Expires",   0);
		return true;
	}

}
