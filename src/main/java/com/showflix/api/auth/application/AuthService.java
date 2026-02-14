package com.showflix.api.auth.application;

import com.showflix.api.auth.application.command.LoginCommand;
import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Layer - 로그인 유스케이스 서비스
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        if (command.getUserid() == null || command.getUserid().isBlank()
                || command.getPassword() == null || command.getPassword().isBlank()) {
            throw new InvalidLoginException("아이디와 비밀번호를 모두 입력해주세요.");
        }

        User user = userRepository.findByUserid(command.getUserid())
                .orElseThrow(() -> new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return new LoginResult(user.getUserid(), user.getUsername(), user.isAdmin());
    }

    public static class LoginResult {
        private final String userid;
        private final String username;
        private final boolean admin;

        public LoginResult(String userid, String username, boolean admin) {
            this.userid = userid;
            this.username = username;
            this.admin = admin;
        }

        public String getUserid() {
            return userid;
        }

        public String getUsername() {
            return username;
        }

        public boolean isAdmin() {
            return admin;
        }
    }
}

