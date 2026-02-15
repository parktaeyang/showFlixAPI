package com.schedulemng.controller.api;

import com.schedulemng.dto.UserDto;
import com.schedulemng.dto.ScheduleTableDTO;
import com.schedulemng.entity.User;
import com.schedulemng.entity.SelectedDate;
import com.schedulemng.entity.Schedule;
import com.schedulemng.security.CustomUserDetails;
import com.schedulemng.service.UserService;
import com.schedulemng.service.SelectedDateService;
import com.schedulemng.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminApiController {

    private final UserService userService;
    private final SelectedDateService selectedDateService;
    private final ScheduleService scheduleService;

    /**
     * 현재 로그인한 관리자 사용자를 검증하고 반환
     */
    private User getCurrentAdminUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        User currentUser = userDetails.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        
        return currentUser;
    }

    /**
     * 성공 응답 생성
     */
    private ResponseEntity<Map<String, Object>> successResponse(Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 성공 응답 생성 (메시지 포함)
     */
    private ResponseEntity<Map<String, Object>> successResponse(String message, Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
            "success", false,
            "error", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 관리자 권한 확인
     */
    @GetMapping("/check-permission")
    public ResponseEntity<Map<String, Object>> checkAdminPermission() {
        try {
            User currentUser = getCurrentAdminUser();
            return successResponse(Map.of(
                "isAdmin", true,
                "userid", currentUser.getUserid(),
                "username", currentUser.getUsername()
            ));
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("관리자 권한 확인 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 다음 사용자 ID 조회
     */
    @GetMapping("/next-userid")
    public ResponseEntity<Map<String, Object>> getNextUserId(@RequestParam String accountType) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            User.AccountType type = User.AccountType.valueOf(accountType.toUpperCase());
            String nextUserId = userService.getNextUserId(type);
            
            return successResponse("다음 사용자 ID를 조회했습니다.", Map.of("nextUserId", nextUserId));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, "잘못된 계정 유형입니다: " + accountType);
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("다음 사용자 ID 조회 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 계정 생성
     */
    @PostMapping("/create-account")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> request) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            // 필수 필드 검증
            String userid = Optional.ofNullable((String) request.get("userid"))
                .orElseThrow(() -> new IllegalArgumentException("사용자 ID는 필수입니다."));
            String username = Optional.ofNullable((String) request.get("username"))
                .orElseThrow(() -> new IllegalArgumentException("사용자명은 필수입니다."));
            String phoneNumber = Optional.ofNullable((String) request.get("phoneNumber"))
                .orElseThrow(() -> new IllegalArgumentException("전화번호는 필수입니다."));
            String accountTypeStr = Optional.ofNullable((String) request.get("accountType"))
                .orElseThrow(() -> new IllegalArgumentException("계정 유형은 필수입니다."));

            User.AccountType accountType = User.AccountType.valueOf(accountTypeStr.toUpperCase());
            
            // 역할 처리
            User.Role role = null;
            if (request.get("role") != null) {
                String roleStr = (String) request.get("role");
                if (!roleStr.isEmpty()) {
                    role = User.Role.valueOf(roleStr.toUpperCase());
                }
            }
            
            User newUser = userService.createUser(userid, username, phoneNumber, accountType, role);
            
            return successResponse("계정이 성공적으로 생성되었습니다.", Map.of(
                "userid", newUser.getUserid(),
                "username", newUser.getUsername(),
                "phoneNumber", newUser.getPhoneNumber(),
                "accountType", newUser.getAccountType().getDisplayName(),
                "role", newUser.getRole() != null ? newUser.getRole().getDisplayName() : "",
                "isAdmin", newUser.isAdmin()
            ));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("계정 생성 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 계정 유형에 따른 사용 가능한 역할 목록 조회
     */
    @GetMapping("/available-roles")
    public ResponseEntity<Map<String, Object>> getAvailableRoles(@RequestParam String accountType) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            User.AccountType type = User.AccountType.valueOf(accountType.toUpperCase());
            List<User.Role> availableRoles = userService.getAvailableRoles(type);
            
            List<Map<String, String>> roleList = availableRoles.stream()
                .map(role -> Map.of(
                    "name", role.name(),
                    "displayName", role.getDisplayName()
                ))
                .toList();
            
            return successResponse(Map.of(
                "accountType", accountType,
                "roles", roleList
            ));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, "잘못된 계정 유형입니다: " + accountType);
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("사용 가능한 역할 조회 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 월별 스케줄 요약 조회
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @RequestParam int year, 
            @RequestParam int month) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            // 년월 유효성 검증
            if (year < 2020 || year > 2030) {
                return errorResponse(HttpStatus.BAD_REQUEST, "년도는 2020-2030 사이여야 합니다.");
            }
            if (month < 1 || month > 12) {
                return errorResponse(HttpStatus.BAD_REQUEST, "월은 1-12 사이여야 합니다.");
            }
            
            List<SelectedDate> monthlyData = selectedDateService.getDatesByMonth(year, month);
            
            return successResponse(Map.of(
                "year", year,
                "month", month,
                "data", monthlyData,
                "totalCount", monthlyData.size()
            ));
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("월별 스케줄 요약 조회 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 모든 사용자 조회
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            List<UserDto> users = userService.getAllUsers()
                    .stream()
                    .map(UserDto::from)
                    .toList();
            
            return successResponse(Map.of(
                "users", users,
                "totalCount", users.size()
            ));
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("사용자 목록 조회 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }



    /**
     * 스케줄 테이블 조회
     */
    @GetMapping("/schedule-table")
    public ResponseEntity<Map<String, Object>> getScheduleTable(
            @RequestParam int year, 
            @RequestParam int month) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            // 년월 유효성 검증
            if (year < 2020 || year > 2030) {
                return errorResponse(HttpStatus.BAD_REQUEST, "년도는 2020-2030 사이여야 합니다.");
            }
            if (month < 1 || month > 12) {
                return errorResponse(HttpStatus.BAD_REQUEST, "월은 1-12 사이여야 합니다.");
            }
            
            ScheduleTableDTO scheduleTable = scheduleService.getScheduleTable(year, month);
            
            return successResponse(scheduleTable);
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("스케줄 테이블 조회 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 스케줄 저장/수정
     */
    @PostMapping("/save-schedule")
    public ResponseEntity<Map<String, Object>> saveSchedule(@RequestBody Map<String, Object> request) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            // 필수 필드 검증
            String dateStr = Optional.ofNullable((String) request.get("date"))
                .orElseThrow(() -> new IllegalArgumentException("날짜는 필수입니다."));
            String username = Optional.ofNullable((String) request.get("username"))
                .orElseThrow(() -> new IllegalArgumentException("사용자명은 필수입니다."));
            
            Double hours = Optional.ofNullable(request.get("hours"))
                .map(value -> {
                    try {
                        return Double.valueOf(value.toString());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("시간은 숫자여야 합니다.");
                    }
                })
                .orElse(0.0);
            
            String remarks = (String) request.get("remarks");

            // 날짜 유효성 검증
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.of(2020, 1, 1)) || date.isAfter(LocalDate.of(2030, 12, 31))) {
                return errorResponse(HttpStatus.BAD_REQUEST, "날짜는 2020-01-01 ~ 2030-12-31 사이여야 합니다.");
            }
            
            // 시간 유효성 검증
            if (hours < 0 || hours > 24) {
                return errorResponse(HttpStatus.BAD_REQUEST, "시간은 0-24 사이여야 합니다.");
            }

            Schedule schedule = scheduleService.saveSchedule(date, username, hours, remarks);
            
            return successResponse("스케줄이 저장되었습니다.", Map.of(
                "id", schedule.getId(),
                "date", schedule.getDate().toString(),
                "username", schedule.getUsername(),
                "hours", schedule.getHours()
            ));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("스케줄 저장 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 스케줄 삭제
     */
    @DeleteMapping("/delete-schedule")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @RequestParam String date, 
            @RequestParam String username) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            // 날짜 유효성 검증
            LocalDate localDate = LocalDate.parse(date);
            if (localDate.isBefore(LocalDate.of(2020, 1, 1)) || localDate.isAfter(LocalDate.of(2030, 12, 31))) {
                return errorResponse(HttpStatus.BAD_REQUEST, "날짜는 2020-01-01 ~ 2030-12-31 사이여야 합니다.");
            }
            
            scheduleService.deleteSchedule(localDate, username);
            
            return successResponse("스케줄이 삭제되었습니다.", null);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("스케줄 삭제 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 일별 특이사항 업데이트
     */
    @PostMapping("/update-daily-remarks")
    public ResponseEntity<Map<String, Object>> updateDailyRemarks(@RequestBody Map<String, Object> request) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            String dateStr = Optional.ofNullable((String) request.get("date"))
                .orElseThrow(() -> new IllegalArgumentException("날짜는 필수입니다."));
            String remarks = (String) request.get("remarks");

            // 날짜 유효성 검증
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.of(2020, 1, 1)) || date.isAfter(LocalDate.of(2030, 12, 31))) {
                return errorResponse(HttpStatus.BAD_REQUEST, "날짜는 2020-01-01 ~ 2030-12-31 사이여야 합니다.");
            }
            
            scheduleService.updateDailyRemarks(date, remarks);
            
            return successResponse("특이사항이 업데이트되었습니다.", null);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("특이사항 업데이트 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 초기화 (관리자용)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, Object> request) {
        try {
            getCurrentAdminUser(); // 관리자 권한 확인
            
            String userid = Optional.ofNullable((String) request.get("userid"))
                .orElseThrow(() -> new IllegalArgumentException("사용자 ID는 필수입니다."));
            
            User updatedUser = userService.resetPasswordByUserid(userid);
            
            return successResponse("비밀번호가 성공적으로 초기화되었습니다.", Map.of(
                "userid", updatedUser.getUserid(),
                "username", updatedUser.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AccessDeniedException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("비밀번호 초기화 중 오류 발생", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
} 