package com.showflix.api.auth.application;

import com.showflix.api.auth.application.command.ChangeMyPasswordCommand;
import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import com.showflix.api.auth.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Layer - 사용자 정보 조회 유스케이스 서비스
 */
@Service
public class UserInfoService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<UserInfoResult> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // CustomUserDetails에서 userid 추출
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userid = userDetails.getUserId();

            return userRepository.findByUserid(userid)
                    .map(user -> new UserInfoResult(
                            user.getUserid(),
                            user.getUsername(),
                            user.isAdmin()
                    ));
        }

        return Optional.empty();
    }

    /**
     * 현재 로그인한 사용자의 비밀번호 변경
     */
    @Transactional
    public void changeMyPassword(ChangeMyPasswordCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userid = userDetails.getUserId();

        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(command.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String encoded = passwordEncoder.encode(command.getNewPassword());
        userRepository.updatePassword(userid, encoded);
    }

    public static class UserInfoResult {
        private final String userid;
        private final String username;
        private final boolean admin;

        public UserInfoResult(String userid, String username, boolean admin) {
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
