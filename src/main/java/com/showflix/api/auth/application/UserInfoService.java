package com.showflix.api.auth.application;

import com.showflix.api.auth.domain.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Layer - 사용자 정보 조회 유스케이스 서비스
 */
@Service
public class UserInfoService {

    private final UserRepository userRepository;

    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserInfoResult> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // CustomUserDetails에서 userid 추출
        if (authentication.getPrincipal() instanceof com.showflix.api.auth.infrastructure.security.CustomUserDetails) {
            com.showflix.api.auth.infrastructure.security.CustomUserDetails userDetails = 
                (com.showflix.api.auth.infrastructure.security.CustomUserDetails) authentication.getPrincipal();
            
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
