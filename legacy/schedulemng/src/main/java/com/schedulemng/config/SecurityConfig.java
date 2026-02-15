package com.schedulemng.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomLoginSuccessHandler customLoginSuccessHandler;
	private final CustomLoginFailureHandler customLoginFailureHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		AuthenticationManager authManager = http.getSharedObject(AuthenticationManager.class);
		CustomUsernamePasswordFilter customFilter = new CustomUsernamePasswordFilter(authManager);
		customFilter.setFilterProcessesUrl("/auth/login");

		http

//				.csrf(csrf -> csrf
//						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//				)
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/register", "/css/**", "/js/**")
						.permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(login -> login
						.loginPage("/login")
						.loginProcessingUrl("/auth/login")
						.successHandler(customLoginSuccessHandler)
						.failureHandler(customLoginFailureHandler)
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}