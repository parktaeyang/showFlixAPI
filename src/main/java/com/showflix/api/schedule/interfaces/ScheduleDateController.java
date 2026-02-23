package com.showflix.api.schedule.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.showflix.api.auth.application.AdminUserService;
import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.infrastructure.security.CustomUserDetails;
import com.showflix.api.schedule.application.AdminNoteService;
import com.showflix.api.schedule.application.ScheduleTimeSlotService;
import com.showflix.api.schedule.application.SelectedDateService;
import com.showflix.api.schedule.application.command.ConfirmScheduleCommand;
import com.showflix.api.schedule.application.command.MonthQueryCommand;
import com.showflix.api.schedule.application.command.SaveSelectedDatesCommand;
import com.showflix.api.schedule.application.command.SaveTimeSlotCommand;
import com.showflix.api.schedule.domain.AdminNote;
import com.showflix.api.schedule.domain.ScheduleRole;
import com.showflix.api.schedule.interfaces.assembler.ScheduleDateAssembler;
import com.showflix.api.schedule.interfaces.dto.MonthDataResponse;
import com.showflix.api.schedule.interfaces.dto.SelectedDateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 선택 날짜 API 엔드포인트
 */
@RestController
@RequestMapping("/api/schedule/dates")
public class ScheduleDateController {

    private final SelectedDateService selectedDateService;
    private final ScheduleTimeSlotService timeSlotService;
    private final AdminNoteService adminNoteService;
    private final AdminUserService adminUserService;

    public ScheduleDateController(SelectedDateService selectedDateService,
                                  ScheduleTimeSlotService timeSlotService,
                                  AdminNoteService adminNoteService,
                                  AdminUserService adminUserService) {
        this.selectedDateService = selectedDateService;
        this.timeSlotService = timeSlotService;
        this.adminNoteService = adminNoteService;
        this.adminUserService = adminUserService;
    }

    /**
     * 월별 데이터 조회
     * GET /api/schedule/dates/month?year=2025&month=2
     */
    @GetMapping("/month")
    public ResponseEntity<MonthDataResponse> getMonthData(
            @RequestParam int year,
            @RequestParam int month) {
        MonthQueryCommand command = new MonthQueryCommand(year, month);
        SelectedDateService.MonthResult result = selectedDateService.getDatesByMonth(command);
        List<SelectedDateResponse> data = ScheduleDateAssembler.toResponseList(result.getData());
        return ResponseEntity.ok(new MonthDataResponse(result.isAdmin(), data));
    }

    /**
     * 선택 날짜 저장
     * POST /api/schedule/dates/save
     * 요청 본문: { "2025-02-15": {}, "2025-02-16": {} }
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveSelectedDates(@RequestBody Map<String, DateSelectionPayload> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).build();
        }
        var user = cud.getUser();
        Map<String, SaveSelectedDatesCommand.DateSelection> dateSelections = payload.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new SaveSelectedDatesCommand.DateSelection(false) // openHope 미사용
                ));
        SaveSelectedDatesCommand command = new SaveSelectedDatesCommand(
                user.getUserid(),
                user.getUsername(),
                "",
                dateSelections
        );
        selectedDateService.saveSelectedDates(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 선택 날짜 단건 삭제 (본인 것만)
     * DELETE /api/schedule/dates?date=2025-02-15
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteSelectedDate(@RequestParam String date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).build();
        }
        selectedDateService.deleteSelectedDate(date, cud.getUser().getUserid());
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // 관리자 전용 - 팝업 API
    // =========================================================

    /**
     * 특정 날짜의 시간표 조회 (관리자 팝업용)
     * GET /api/schedule/dates/time-slots?date=2025-02-15
     */
    @GetMapping("/time-slots")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScheduleTimeSlotService.TimeSlotResult>> getTimeSlots(
            @RequestParam String date) {
        List<ScheduleTimeSlotService.TimeSlotResult> results = timeSlotService.getTimeSlotsByDate(date)
                .stream()
                .map(ScheduleTimeSlotService.TimeSlotResult::new)
                .toList();
        return ResponseEntity.ok(results);
    }

    /**
     * 시간표 저장 (관리자 팝업 - 배치 저장)
     * POST /api/schedule/dates/time-slots/save
     * 요청 본문: { "date": "2025-02-15", "slots": [ { "timeSlot": "16:00", "theme": "테마", "performer": "홍길동,김철수" } ] }
     */
    @PostMapping("/time-slots/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveTimeSlots(@RequestBody TimeSlotsPayload payload) {
        if (payload.date() == null || payload.slots() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<SaveTimeSlotCommand> commands = payload.slots().stream()
                .map(s -> new SaveTimeSlotCommand(
                        payload.date(),
                        s.timeSlot(),
                        s.theme(),
                        s.performer()
                ))
                .toList();
        timeSlotService.saveTimeSlots(commands);
        return ResponseEntity.ok().build();
    }

    /**
     * 스케줄 확정/취소 (관리자 팝업)
     * POST /api/schedule/dates/confirm
     * 요청 본문: { "date": "2025-02-15", "confirmed": "Y" }
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmSchedule(@RequestBody ConfirmPayload payload) {
        if (payload.date() == null || payload.confirmed() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!payload.confirmed().equals("Y") && !payload.confirmed().equals("N")) {
            return ResponseEntity.badRequest().build();
        }
        timeSlotService.confirmSchedule(new ConfirmScheduleCommand(payload.date(), payload.confirmed()));
        return ResponseEntity.ok().build();
    }

    /**
     * 출근자 역할/비고 저장 (관리자 팝업)
     * POST /api/schedule/dates/roles/save
     * 요청 본문: [ { "date": "2025-02-15", "userId": "user1", "role": "MC", "remarks": "비고" } ]
     */
    @PostMapping("/roles/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveRoles(@RequestBody List<RoleUpdatePayload> updates) {
        for (RoleUpdatePayload u : updates) {
            selectedDateService.updateRoleAndRemarks(u.date(), u.userId(), u.role(), u.remarks());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 역할 목록 조회 (드롭다운용)
     * GET /api/schedule/dates/roles
     * 인증 필요, 관리자 불필요
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleOptionResponse>> getRoles() {
        List<RoleOptionResponse> roles = Arrays.stream(ScheduleRole.values())
                .map(r -> new RoleOptionResponse(r.name(), r.getDisplayName()))
                .toList();
        return ResponseEntity.ok(roles);
    }

    // =========================================================
    // 관리자 전용 - 사용자 추가/삭제 API
    // =========================================================

    /**
     * 전체 사용자 목록 조회 (관리자 팝업 - 사용자 추가용)
     * GET /api/schedule/dates/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListResponse>> listUsers() {
        List<User> users = adminUserService.getAllUsers();
        List<UserListResponse> responses = users.stream()
                .map(u -> new UserListResponse(u.getUserid(), u.getUsername()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 관리자가 특정 날짜에 사용자 추가
     * POST /api/schedule/dates/add-user
     * 요청 본문: { "date": "2025-02-15", "userId": "user1", "userName": "홍길동", "role": "DOOR" }
     */
    @PostMapping("/add-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addUserToDate(@RequestBody AddUserPayload payload) {
        if (payload.date() == null || payload.userId() == null) {
            return ResponseEntity.badRequest().build();
        }
        selectedDateService.addUserToDate(
                payload.date(), payload.userId(), payload.userName(), payload.role()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자가 특정 날짜에서 사용자 삭제
     * DELETE /api/schedule/dates/selection?date=2025-02-15&userId=user1
     */
    @DeleteMapping("/selection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserFromDate(
            @RequestParam String date,
            @RequestParam String userId) {
        int deleted = selectedDateService.deleteSelectedDate(date, userId);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // 공지사항 API
    // =========================================================

    /**
     * 공지사항 조회
     * GET /api/schedule/dates/admin-note
     */
    @GetMapping("/admin-note")
    public ResponseEntity<AdminNote> getAdminNote() {
        AdminNote note = adminNoteService.getAdminNote();
        if (note == null) {
            return ResponseEntity.ok(new AdminNote("GLOBAL", "", "", ""));
        }
        return ResponseEntity.ok(note);
    }

    /**
     * 공지사항 저장 (관리자 전용)
     * POST /api/schedule/dates/admin-note
     * 요청 본문: { "content": "공지사항 내용" }
     */
    @PostMapping("/admin-note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> saveAdminNote(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).build();
        }
        adminNoteService.saveAdminNote(content, cud.getUser().getUsername());
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // Payload records
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateSelectionPayload(boolean openHope) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeSlotsPayload(String date, List<SlotItem> slots) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SlotItem(String timeSlot, String theme, String performer) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfirmPayload(String date, String confirmed) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoleUpdatePayload(String date, String userId, String role, String remarks) {}

    public record RoleOptionResponse(String value, String label) {}

    public record UserListResponse(String userId, String userName) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddUserPayload(String date, String userId, String userName, String role) {}
}
