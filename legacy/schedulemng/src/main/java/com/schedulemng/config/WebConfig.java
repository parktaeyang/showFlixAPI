package com.schedulemng.config;

import com.schedulemng.interceptor.NoCacheInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final NoCacheInterceptor noCacheInterceptor;

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		registry.addInterceptor(noCacheInterceptor)
				.addPathPatterns("/", "/login", "/schedule/calendar");
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
