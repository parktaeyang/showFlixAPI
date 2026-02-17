package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.ScheduleException;
import com.showflix.api.schedule.application.ScheduleService;
import com.showflix.api.schedule.application.command.DeleteScheduleCommand;
import com.showflix.api.schedule.application.command.SaveScheduleCommand;
import com.showflix.api.schedule.application.command.ScheduleTableQueryCommand;
import com.showflix.api.schedule.application.command.UpdateDailyRemarksCommand;
import com.showflix.api.schedule.interfaces.assembler.ScheduleAssembler;
import com.showflix.api.schedule.interfaces.dto.SaveScheduleRequest;
import com.showflix.api.schedule.interfaces.dto.ScheduleTableResponse;
import com.showflix.api.schedule.interfaces.dto.UpdateDailyRemarksRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Interfaces Layer - Schedule REST Controller (관리자 전용)
 * 경로: /api/admin
 * SecurityConfig에서 /api/admin/** → hasRole("ADMIN") 으로 보호됨
 */
@RestController
@RequestMapping("/api/admin")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * GET /api/admin/schedule-table?year=2025&month=1
     * 월별 스케줄 테이블 조회
     */
    @GetMapping("/schedule-table")
    public ResponseEntity<ScheduleTableResponse> getScheduleTable(
            @RequestParam int year,
            @RequestParam int month) {
        ScheduleTableQueryCommand command = ScheduleAssembler.toTableQueryCommand(year, month);
        ScheduleService.ScheduleTableResult result = scheduleService.getScheduleTable(command);
        return ResponseEntity.ok(ScheduleAssembler.toTableResponse(result));
    }

    /**
     * POST /api/admin/save-schedule
     * 스케줄 저장/수정 (date + username 기준 upsert)
     * Body: { "date": "2025-01-15", "username": "홍길동", "hours": 8.0, "remarks": "" }
     */
    @PostMapping("/save-schedule")
    public ResponseEntity<?> saveSchedule(@RequestBody SaveScheduleRequest request) {
        if (request.getDate() == null || request.getDate().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errorMessage", "date는 필수입니다."));
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errorMessage", "username은 필수입니다."));
        }
        try {
            SaveScheduleCommand command = ScheduleAssembler.toSaveCommand(request);
            scheduleService.saveSchedule(command);
            return ResponseEntity.ok().build();
        } catch (ScheduleException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errorMessage", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/delete-schedule?date=2025-01-15&username=홍길동
     * 스케줄 삭제
     */
    @DeleteMapping("/delete-schedule")
    public ResponseEntity<?> deleteSchedule(
            @RequestParam String date,
            @RequestParam String username) {
        if (date == null || date.isBlank() || username == null || username.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errorMessage", "date와 username은 필수입니다."));
        }
        try {
            DeleteScheduleCommand command = ScheduleAssembler.toDeleteCommand(date, username);
            scheduleService.deleteSchedule(command);
            return ResponseEntity.ok().build();
        } catch (ScheduleException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorMessage", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/update-daily-remarks
     * 일별 특이사항 일괄 업데이트
     * Body: { "date": "2025-01-15", "remarks": "특이사항 내용" }
     */
    @PostMapping("/update-daily-remarks")
    public ResponseEntity<?> updateDailyRemarks(
            @RequestBody UpdateDailyRemarksRequest request) {
        if (request.getDate() == null || request.getDate().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errorMessage", "date는 필수입니다."));
        }
        UpdateDailyRemarksCommand command =
                ScheduleAssembler.toUpdateRemarksCommand(request);
        scheduleService.updateDailyRemarks(command);
        return ResponseEntity.ok().build();
    }
}
