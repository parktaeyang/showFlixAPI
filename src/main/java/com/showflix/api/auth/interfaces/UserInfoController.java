package com.showflix.api.auth.interfaces;

import com.showflix.api.auth.application.UserInfoService;
import com.showflix.api.auth.application.command.ChangeMyPasswordCommand;
import com.showflix.api.auth.interfaces.dto.UserInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interfaces Layer - 사용자 정보 조회 / 비밀번호 변경 HTTP 엔드포인트
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

    record ChangePasswordRequest(String currentPassword, String newPassword) {}

    /**
     * 본인 비밀번호 변경
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public ResponseEntity<?> changeMyPassword(@RequestBody ChangePasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().trim().length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "새 비밀번호는 4자 이상이어야 합니다."));
        }

        try {
            ChangeMyPasswordCommand command = new ChangeMyPasswordCommand(
                    request.currentPassword(),
                    request.newPassword()
            );
            userInfoService.changeMyPassword(command);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "오류가 발생했습니다."));
        }
    }
}
