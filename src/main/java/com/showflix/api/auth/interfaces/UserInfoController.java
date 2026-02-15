package com.showflix.api.auth.interfaces;

import com.showflix.api.auth.application.UserInfoService;
import com.showflix.api.auth.interfaces.dto.UserInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Interfaces Layer - 사용자 정보 조회 HTTP 엔드포인트
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/info")
    public ResponseEntity<?> getCurrentUserInfo() {
        return userInfoService.getCurrentUserInfo()
                .map(result -> ResponseEntity.ok(new UserInfoResponse(
                        result.getUserid(),
                        result.getUsername(),
                        result.isAdmin()
                )))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
