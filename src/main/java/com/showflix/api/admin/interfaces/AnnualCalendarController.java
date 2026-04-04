package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.AnnualCalendarService;
import com.showflix.api.admin.domain.AnnualCalendarEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 연간일정캘린더 API
 * sf_time_slot 테이블 기반 연간 공연 일정 집계
 * 경로: /api/admin/annual-calendar
 */
@RestController
@RequestMapping("/api/admin/annual-calendar")
@PreAuthorize("hasRole('ADMIN')")
public class AnnualCalendarController {

    private final AnnualCalendarService service;

    public AnnualCalendarController(AnnualCalendarService service) {
        this.service = service;
    }

    record AnnualCalendarResponse(String scheduleDate, String timeSlot,
                                  String theme, String performer, String confirmed) {}

    /**
     * 연간 공연 일정 조회
     * GET /api/admin/annual-calendar?year=2026
     */
    @GetMapping
    public ResponseEntity<List<AnnualCalendarResponse>> getByYear(@RequestParam int year) {
        List<AnnualCalendarResponse> responses = service.getByYear(year).stream()
                .map(e -> new AnnualCalendarResponse(
                        e.getScheduleDate(), e.getTimeSlot(),
                        e.getTheme(), e.getPerformer(), e.getConfirmed()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
