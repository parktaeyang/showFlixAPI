package com.showflix.api.schedule.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.showflix.api.auth.infrastructure.security.CustomUserDetails;
import com.showflix.api.schedule.application.ScheduleTimeSlotService;
import com.showflix.api.schedule.application.SelectedDateService;
import com.showflix.api.schedule.application.command.ConfirmScheduleCommand;
import com.showflix.api.schedule.application.command.MonthQueryCommand;
import com.showflix.api.schedule.application.command.SaveSelectedDatesCommand;
import com.showflix.api.schedule.application.command.SaveTimeSlotCommand;
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

    public ScheduleDateController(SelectedDateService selectedDateService,
                                  ScheduleTimeSlotService timeSlotService) {
        this.selectedDateService = selectedDateService;
        this.timeSlotService = timeSlotService;
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
}
