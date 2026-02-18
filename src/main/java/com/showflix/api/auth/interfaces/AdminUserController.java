package com.showflix.api.auth.interfaces;

import com.showflix.api.auth.application.AdminUserService;
import com.showflix.api.auth.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 관리자 계정관리 API
 * 경로: /api/admin/users/**
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // DTO 정의 (record)
    record UserSummaryResponse(String userid, String username, boolean admin) {}
    record CreateUserRequest(String userid, String username, String password, boolean admin) {}
    record UpdateUserRequest(String username, boolean admin) {}
    record ChangePasswordRequest(String newPassword) {}

    /**
     * 전체 사용자 목록 조회
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        List<User> users = adminUserService.getAllUsers();
        List<UserSummaryResponse> responses = users.stream()
                .map(u -> new UserSummaryResponse(u.getUserid(), u.getUsername(), u.isAdmin()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 신규 계정 추가
     * POST /api/admin/users
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody CreateUserRequest request) {
        adminUserService.createUser(
                request.userid(),
                request.username(),
                request.password(),
                request.admin()
        );
        return ResponseEntity.ok(Map.of("message", "계정이 추가되었습니다."));
    }

    /**
     * 계정 수정 (username, isAdmin)
     * PUT /api/admin/users/{userid}
     */
    @PutMapping("/{userid}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable String userid,
            @RequestBody UpdateUserRequest request) {
        adminUserService.updateUser(userid, request.username(), request.admin());
        return ResponseEntity.ok(Map.of("message", "계정이 수정되었습니다."));
    }

    /**
     * 비밀번호 변경
     * PUT /api/admin/users/{userid}/password
     */
    @PutMapping("/{userid}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable String userid,
            @RequestBody ChangePasswordRequest request) {
        adminUserService.changePassword(userid, request.newPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }

    /**
     * 계정 삭제 (본인 계정 삭제 불가)
     * DELETE /api/admin/users/{userid}
     */
    @DeleteMapping("/{userid}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable String userid,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserid = userDetails.getUsername();
        adminUserService.deleteUser(userid, currentUserid);
        return ResponseEntity.ok(Map.of("message", "계정이 삭제되었습니다."));
    }
}
