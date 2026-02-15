package com.showflix.api;

import com.showflix.api.auth.infrastructure.security.CustomLoginFailureHandler;
import com.showflix.api.auth.infrastructure.security.CustomLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
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
                        // 공개 경로
                        .requestMatchers("/", "/index.html", "/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        // 인증 필요 경로
                        .requestMatchers("/schedule/**").authenticated()
                        .requestMatchers("/api/user/info").authenticated()
                        // 나머지 API는 일단 허용 (추후 필요시 조정)
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
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


