package com.showflix.api;

import com.showflix.api.auth.infrastructure.security.CustomLoginFailureHandler;
import com.showflix.api.auth.infrastructure.security.CustomLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    public SecurityConfig(CustomLoginSuccessHandler customLoginSuccessHandler,
                         CustomLoginFailureHandler customLoginFailureHandler) {
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.customLoginFailureHandler = customLoginFailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 공개 경로 (index.html은 로그인 페이지로 공개)
                        .requestMatchers("/", "/index.html", "/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        // .html 직접 접근 차단 (index.html 제외 - 위에서 permitAll로 먼저 매칭됨)
                        .requestMatchers("/**/*.html").denyAll()
                        // 인증 필요 경로
                        .requestMatchers("/schedule/**").authenticated()
                        .requestMatchers("/api/user/info").authenticated()
                        .requestMatchers("/api/schedule/**").authenticated()
                        // 관리자 전용 경로
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // /api/** 경로는 미인증 시 로그인 페이지로 리다이렉트 대신 401 반환
                        // (fetch 호출 시 리다이렉트 루프 방지)
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        // 그 외 경로는 formLogin의 loginPage 설정으로 자동 처리됨
                )
                .formLogin(login -> login
                        .loginPage("/index.html")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler(customLoginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index.html")
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

}


